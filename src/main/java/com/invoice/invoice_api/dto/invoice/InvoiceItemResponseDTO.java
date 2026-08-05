package com.invoice.invoice_api.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceItemResponseDTO(
        Long id,

        Long workLogId,

        LocalDate workDate,

        String projectName,

        String positionName,

        String description,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
