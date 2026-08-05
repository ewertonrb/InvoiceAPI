package com.invoice.invoice_api.dto.invoice;

import com.invoice.invoice_api.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceSummaryResponseDTO(
        Long id,

        String invoiceNumber,

        Long workerProfileId,

        String workerName,

        LocalDate periodStart,

        LocalDate periodEnd,

        LocalDate issueDate,

        LocalDate dueDate,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount,

        InvoiceStatus status,

        Integer itemCount,

        LocalDateTime createdAt
) {
}
