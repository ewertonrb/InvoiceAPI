package com.invoice.invoice_api.dto.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoicePdfDTO(
        String companyName,
        String companyAbn,
        String companyAddress,
        String companyPhone,
        String companyEmail,

        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate periodStart,
        LocalDate periodEnd,

        String workerName,
        String workerAbn,
        Boolean workerGstRegistered,
        Boolean gstApplied,

        String bankName,
        String accountName,
        String bsb,
        String accountNumber,

        BigDecimal subtotalAmount,
        BigDecimal gstAmount,
        BigDecimal totalAmount,

        List<InvoicePdfItemDTO> items
) {
}
