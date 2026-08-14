package com.invoice.invoice_api.service.pdf;

import com.invoice.invoice_api.dto.pdf.InvoicePdfDTO;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.InvoicePdfMapper;
import com.invoice.invoice_api.model.Invoice;
import com.invoice.invoice_api.repository.InvoiceRepository;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyContext companyContext;
    private final HtmlTemplateRenderer htmlTemplateRenderer;
    private final PdfGenerator pdfGenerator;
    private final AuthenticatedUserService authenticatedUserService;

    public InvoicePdfService(
            InvoiceRepository invoiceRepository,
            CompanyContext companyContext,
            HtmlTemplateRenderer htmlTemplateRenderer,
            PdfGenerator pdfGenerator,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.invoiceRepository =
                invoiceRepository;

        this.companyContext =
                companyContext;

        this.htmlTemplateRenderer =
                htmlTemplateRenderer;

        this.pdfGenerator =
                pdfGenerator;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    public byte[] generate(Long invoiceId) {
        Long companyId =
                companyContext.getCompanyId();

        Invoice invoice = (companyContext.getRole() == CompanyRole.WORKER
                        ? invoiceRepository.findByIdAndWorkerAppUserIdAndCompanyIdWithItems(invoiceId, authenticatedUserService.getCurrentUserId(), companyId)
                        : invoiceRepository.findByIdAndCompanyIdWithItems(invoiceId, companyId))
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invoice not found with ID: "
                                                + invoiceId
                                )
                        );

        /*
         * For the V1, only issued or paid invoices can be
         * downloaded as official PDFs.
         */
        if (!invoice.isIssued() && !invoice.isPaid()) {
            throw new BusinessException(
                    "Only issued or paid invoices can generate an official PDF."
            );
        }

        InvoicePdfDTO invoicePdfDTO = InvoicePdfMapper.toPdfDTO(invoice);

        String html = htmlTemplateRenderer.renderInvoice(invoicePdfDTO);

        return pdfGenerator.generateFromHtml(html);
    }

    @Transactional(readOnly = true)
    public String getFilename(
            Long invoiceId
    ) {
        Long companyId = companyContext.getCompanyId();

        Invoice invoice = (companyContext.getRole() == CompanyRole.WORKER
                        ? invoiceRepository.findByIdAndWorkerProfile_AppUser_IdAndCompanyId(invoiceId, authenticatedUserService.getCurrentUserId(), companyId)
                        : invoiceRepository.findByIdAndCompanyId(invoiceId, companyId))
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invoice not found with ID: "
                                                + invoiceId
                                )
                        );

        return sanitizeFilename(
                invoice.getInvoiceNumber()
        ) + ".pdf";
    }

    private String sanitizeFilename(String value) {

        if (value == null || value.isBlank()) {
            return "invoice";
        }

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
}
