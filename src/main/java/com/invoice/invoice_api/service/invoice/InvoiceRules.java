package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.model.Invoice;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class InvoiceRules {
    private InvoiceRules() {}

    /*
     * ============================================================
     * ISSUE
     * ============================================================
     */

    public static void issue(Invoice invoice, LocalDate issueDate, LocalDate dueDate) {
        invoice.setStatus(InvoiceStatus.ISSUED);

        invoice.setIssueDate(
                issueDate
        );

        invoice.setDueDate(
                dueDate
        );

        invoice.setIssuedAt(
                LocalDateTime.now()
        );

        invoice.setPaidAt(null);
        invoice.setCancelledAt(null);
    }

    /*
     * ============================================================
     * PAYMENT
     * ============================================================
     */

    public static void markAsPaid(
            Invoice invoice
    ) {
        invoice.setStatus(InvoiceStatus.PAID);

        invoice.setPaidAt(LocalDateTime.now()
        );
    }

    /*
     * ============================================================
     * CANCELLATION
     * ============================================================
     */

    public static void cancel(Invoice invoice) {

        invoice.setStatus(InvoiceStatus.CANCELLED);

        invoice.setCancelledAt(LocalDateTime.now());

    }
}
