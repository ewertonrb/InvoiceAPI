package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.InvoiceResponseDTO;
import com.invoice.invoice_api.dto.invoice.InvoiceSummaryResponseDTO;
import com.invoice.invoice_api.dto.invoice.IssueInvoiceRequestDTO;
import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.InvoiceMapper;
import com.invoice.invoice_api.model.Invoice;
import com.invoice.invoice_api.model.InvoiceItem;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.repository.InvoiceRepository;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.service.workLog.WorkLogRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final WorkLogRepository workLogRepository;
    private final CompanyContext companyContext;
    private final InvoiceValidator invoiceValidator;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            WorkLogRepository workLogRepository,
            CompanyContext companyContext,
            InvoiceValidator invoiceValidator
    ) {
        this.invoiceRepository = invoiceRepository;
        this.workLogRepository = workLogRepository;
        this.companyContext = companyContext;
        this.invoiceValidator = invoiceValidator;
    }

    /*
     * ============================================================
     * READ
     * ============================================================
     */

    @Transactional(readOnly = true)
    public InvoiceResponseDTO findById(Long invoiceId) {

        Long companyId = getCurrentCompanyId();

        Invoice invoice = findInvoiceWithItems(invoiceId, companyId);

        return InvoiceMapper.toResponseDTO(invoice);
    }

    @Transactional(readOnly = true)

    public List<InvoiceSummaryResponseDTO> findAll(InvoiceStatus status) {

        Long companyId = getCurrentCompanyId();

        List<Invoice> invoices;

        if (status == null) {
            invoices =
                    invoiceRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId);
        } else {
            invoices = invoiceRepository.findAllByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
        }

        return invoices.stream()
                .map(InvoiceMapper::toSummaryResponseDTO)
                .toList();
    }

    /*
     * ============================================================
     * ISSUE
     * ============================================================
     */

    @Transactional
    public InvoiceResponseDTO issue(Long invoiceId, IssueInvoiceRequestDTO request) {

        Long companyId = getCurrentCompanyId();

        Invoice invoice = findInvoiceWithItems(invoiceId, companyId);

        invoiceValidator.validateCanBeIssued(invoice, request);

        /*
         * The invoice is officially issued first in memory.
         * The entire operation remains atomic because the method
         * runs inside a single database transaction.
         */
        InvoiceRules.issue(invoice, request.issueDate(), request.dueDate());

        List<WorkLog> workLogs = invoice.getItems()
                        .stream()
                        .map(InvoiceItem::getWorkLog)
                        .toList();

        for (WorkLog workLog : workLogs) {WorkLogRules.markAsInvoiced(workLog);
        }

        workLogRepository.saveAll(workLogs);

        Invoice issuedInvoice = invoiceRepository.saveAndFlush(invoice);

        return InvoiceMapper.toResponseDTO(issuedInvoice);
    }

    /*
     * ============================================================
     * ENTITY LOOKUPS
     * ============================================================
     */

    private Invoice findInvoiceWithItems(Long invoiceId, Long companyId) {

        return invoiceRepository.findByIdAndCompanyIdWithItems(invoiceId, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice not found with ID: "
                                        + invoiceId
                        )
                );
    }

    /*
     * ============================================================
     * COMPANY CONTEXT
     * ============================================================
     */

    private Long getCurrentCompanyId() {
        return companyContext.getCompanyId();
    }

    /*
     * ============================================================
     * PAYMENT
     * ============================================================
     */

    @Transactional
    public InvoiceResponseDTO markAsPaid(
            Long invoiceId
    ) {
        Long companyId =
                getCurrentCompanyId();

        Invoice invoice =
                findInvoiceWithItems(
                        invoiceId,
                        companyId
                );

        invoiceValidator.validateCanBeMarkedAsPaid(
                invoice
        );

        InvoiceRules.markAsPaid(
                invoice
        );

        Invoice paidInvoice =
                invoiceRepository.saveAndFlush(
                        invoice
                );

        return InvoiceMapper.toResponseDTO(
                paidInvoice
        );
    }

    /*
     * ============================================================
     * CANCELLATION
     * ============================================================
     */

    @Transactional
    public InvoiceResponseDTO cancel(
            Long invoiceId
    ) {
        Long companyId =
                getCurrentCompanyId();

        Invoice invoice =
                findInvoiceWithItems(
                        invoiceId,
                        companyId
                );

        invoiceValidator.validateCanBeCancelled(
                invoice
        );

        /*
         * Removing the items releases the approved WorkLogs.
         * Because orphanRemoval is enabled, the corresponding
         * invoice_items rows are deleted automatically.
         */
        invoice.clearItems();

        invoice.setSubtotalAmount(
                java.math.BigDecimal.ZERO
        );

        invoice.setGstAmount(
                java.math.BigDecimal.ZERO
        );

        invoice.setTotalAmount(
                java.math.BigDecimal.ZERO
        );

        InvoiceRules.cancel(
                invoice
        );

        Invoice cancelledInvoice =
                invoiceRepository.saveAndFlush(
                        invoice
                );

        return InvoiceMapper.toResponseDTO(
                cancelledInvoice
        );
    }
}
