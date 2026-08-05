package com.invoice.invoice_api.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoicePeriodPreviewResponseDTO(

        LocalDate periodStart,

        LocalDate periodEnd,

        Integer workerCount,

        Integer readyWorkerCount,

        Integer blockedWorkerCount,

        Integer workLogCount,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount,

        List<InvoiceWorkerPreviewDTO> workers
) {
}
