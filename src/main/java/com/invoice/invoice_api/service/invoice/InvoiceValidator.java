package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.IssueInvoiceRequestDTO;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.Invoice;
import com.invoice.invoice_api.model.InvoiceItem;
import com.invoice.invoice_api.model.WorkLog;
import org.springframework.stereotype.Component;

@Component
public class InvoiceValidator {
    /*
     * ============================================================
     * ISSUE
     * ============================================================
     */

    public void validateCanBeIssued(Invoice invoice, IssueInvoiceRequestDTO request) {
        if (invoice == null) {
            throw new BusinessException(
                    "Invoice is required."
            );
        }

        if (!invoice.canBeIssued()) {
            throw new BusinessException(
                    "Only draft invoices can be issued."
            );
        }

        validateIssueRequest(request);
        validateInvoiceItems(invoice);
    }

    private void validateIssueRequest(
            IssueInvoiceRequestDTO request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Issue invoice request is required."
            );
        }

        if (
                request.issueDate() == null || request.dueDate() == null
        ) {
            throw new BusinessException(
                    "Issue date and due date are required."
            );
        }

        if (
                request.dueDate().isBefore(request.issueDate())
        ) {
            throw new BusinessException(
                    "Due date cannot be before issue date."
            );
        }
    }

    private void validateInvoiceItems(Invoice invoice) {
        if (
                invoice.getItems() == null || invoice.getItems().isEmpty()
        ) {
            throw new BusinessException(
                    "An invoice without items cannot be issued."
            );
        }

        for (InvoiceItem item : invoice.getItems()) {
            WorkLog workLog = item.getWorkLog();

            if (workLog == null) {
                throw new BusinessException(
                        "Every invoice item must reference a WorkLog."
                );
            }

            if (!workLog.isApproved()) {
                throw new BusinessException(
                        "Every WorkLog must be approved before the invoice is issued."
                );
            }

            if (!workLog.hasFinancialSnapshot()) {
                throw new BusinessException(
                        "Every WorkLog must contain a financial snapshot."
                );
            }
        }
    }
    /*
     * ============================================================
     * PAYMENT
     * ============================================================
     */

    public void validateCanBeMarkedAsPaid(
            Invoice invoice
    ) {
        if (invoice == null) {
            throw new BusinessException(
                    "Invoice is required."
            );
        }

        if (!invoice.canBeMarkedAsPaid()) {
            throw new BusinessException(
                    "Only issued invoices can be marked as paid."
            );
        }
    }

    /*
     * ============================================================
     * CANCELLATION
     * ============================================================
     */

    public void validateCanBeCancelled(
            Invoice invoice
    ) {
        if (invoice == null) {
            throw new BusinessException(
                    "Invoice is required."
            );
        }

        if (!invoice.canBeCancelled()) {
            throw new BusinessException(
                    "Only draft invoices can be cancelled."
            );
        }
    }
}
