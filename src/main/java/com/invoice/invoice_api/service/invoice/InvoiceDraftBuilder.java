package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceDraftBuilder {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final InvoiceNumberGenerator invoiceNumberGenerator;

    private final InvoiceItemBuilder invoiceItemBuilder;

    public InvoiceDraftBuilder(InvoiceNumberGenerator invoiceNumberGenerator, InvoiceItemBuilder invoiceItemBuilder) {
        this.invoiceNumberGenerator = invoiceNumberGenerator;
        this.invoiceItemBuilder = invoiceItemBuilder;
    }

    public Invoice build(WorkerProfile workerProfile, List<WorkLog> workLogs, LocalDate periodStart, LocalDate periodEnd) {

        validateInput(workerProfile, workLogs, periodStart, periodEnd);

        Company company = resolveCompany(workLogs);

        Invoice invoice = new Invoice();

        invoice.setCompany(company);

        invoice.setWorkerProfile(workerProfile);

        invoice.setInvoiceNumber(invoiceNumberGenerator.generate());

        invoice.setPeriodStart(periodStart);

        invoice.setPeriodEnd(periodEnd);

        invoice.setStatus(InvoiceStatus.DRAFT);

        /*
         * Draft invoices are not officially issued yet.
         */
        invoice.setIssueDate(null);
        invoice.setDueDate(null);
        invoice.setIssuedAt(null);
        invoice.setPaidAt(null);
        invoice.setCancelledAt(null);
        invoice.setPdfPath(null);
        invoice.setNotes(null);

        addInvoiceItems(invoice, workLogs);

        updateInvoiceTotals(invoice);

        return invoice;
    }

    /*
     * ============================================================
     * ITEM CREATION
     * ============================================================
     */

    private void addInvoiceItems(Invoice invoice, List<WorkLog> workLogs) {
        for (WorkLog workLog : workLogs) {

            InvoiceItem invoiceItem = invoiceItemBuilder.build(workLog);

            invoice.addItem(invoiceItem);
        }
    }

    /*
     * ============================================================
     * TOTALS
     * ============================================================
     */

    private void updateInvoiceTotals(
            Invoice invoice
    ) {
        BigDecimal subtotalAmount = invoice.getItems()
                        .stream()
                        .map(InvoiceItem::getSubtotalAmount)
                        .filter(value -> value != null)
                        .reduce(ZERO, BigDecimal::add);

        BigDecimal gstAmount = invoice.getItems()
                        .stream()
                        .map(InvoiceItem::getGstAmount)
                        .filter(value -> value != null)
                        .reduce(ZERO, BigDecimal::add);

        BigDecimal totalAmount = invoice.getItems()
                        .stream()
                        .map(InvoiceItem::getTotalAmount)
                        .filter(value -> value != null)
                        .reduce(ZERO, BigDecimal::add);

        invoice.setSubtotalAmount(money(subtotalAmount));

        invoice.setGstAmount(money(gstAmount));

        invoice.setTotalAmount(money(totalAmount));
    }

    /*
     * ============================================================
     * COMPANY
     * ============================================================
     */

    private Company resolveCompany(List<WorkLog> workLogs) {
        Company company = workLogs.get(0)
                        .getProjectPosition()
                        .getProject()
                        .getCompany();

        boolean containsDifferentCompany = workLogs.stream()
                        .anyMatch(workLog ->
                                !workLog.getProjectPosition()
                                        .getProject()
                                        .getCompany()
                                        .getId()
                                        .equals(company.getId())
                        );

        if (containsDifferentCompany) {throw new BusinessException(
                    "All work logs in an invoice must belong to the same company."
            );
        }

        return company;
    }

    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validateInput(WorkerProfile workerProfile, List<WorkLog> workLogs, LocalDate periodStart, LocalDate periodEnd) {
        if (workerProfile == null) {throw new BusinessException(
                    "Worker profile is required to create an invoice draft."
            );
        }

        if (
                workLogs == null || workLogs.isEmpty()
        ) {
            throw new BusinessException(
                    "At least one work log is required to create an invoice draft."
            );
        }

        if (
                periodStart == null || periodEnd == null
        ) {
            throw new BusinessException(
                    "Invoice period start and end are required."
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new BusinessException("Invoice period start cannot be after period end.");
        }

        validateWorkerOwnership(workerProfile, workLogs);

        validateWorkLogDates(workLogs, periodStart, periodEnd);
    }

    private void validateWorkerOwnership(WorkerProfile workerProfile, List<WorkLog> workLogs) {

        boolean containsDifferentWorker = workLogs.stream()
                        .anyMatch(workLog ->
                                !workLog.getWorkerProfile()
                                        .getId()
                                        .equals(workerProfile.getId())
                        );

        if (containsDifferentWorker) {
            throw new BusinessException(
                    "All work logs in an invoice must belong to the same worker."
            );
        }
    }

    private void validateWorkLogDates(List<WorkLog> workLogs, LocalDate periodStart, LocalDate periodEnd) {

        boolean containsDateOutsidePeriod = workLogs.stream()
                        .anyMatch(workLog ->
                                workLog.getWorkDate()
                                        .isBefore(periodStart) || workLog.getWorkDate()
                                        .isAfter(periodEnd)
                        );

        if (containsDateOutsidePeriod) {
            throw new BusinessException(
                    "All work logs must be inside the selected invoice period."
            );
        }
    }

    /*
     * ============================================================
     * MONEY
     * ============================================================
     */

    private BigDecimal money(BigDecimal value) {

        if (value == null) {
            return ZERO;
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
