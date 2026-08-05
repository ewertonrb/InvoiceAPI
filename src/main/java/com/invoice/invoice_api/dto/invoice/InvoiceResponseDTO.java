package com.invoice.invoice_api.dto.invoice;

import com.invoice.invoice_api.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponseDTO(
        Long id,

        String invoiceNumber,

        Long companyId,

        String companyName,

        Long workerProfileId,

        Long appUserId,

        String workerName,

        String workerEmail,

        String workerAbn,

        Boolean workerGstRegistered,

        LocalDate periodStart,

        LocalDate periodEnd,

        LocalDate issueDate,

        LocalDate dueDate,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount,

        InvoiceStatus status,

        String notes,

        String pdfPath,

        LocalDateTime issuedAt,

        LocalDateTime paidAt,

        LocalDateTime cancelledAt,

        Integer itemCount,

        List<InvoiceItemResponseDTO> items,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
