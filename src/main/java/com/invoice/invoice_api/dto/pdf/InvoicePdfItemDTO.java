package com.invoice.invoice_api.dto.pdf;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoicePdfItemDTO(
        LocalDate workDate,

        String projectName,

        String positionName,

        String description,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount
) {
}
