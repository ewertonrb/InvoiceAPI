package com.invoice.invoice_api.dto.invoice;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceWorkerPreviewDTO(
        Long workerProfileId,

        Long appUserId,

        String workerName,

        String workerEmail,

        String workerAbn,

        Boolean gstRegistered,

        Integer workLogCount,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount,

        Boolean readyToGenerate,

        List<InvoicePreviewProblemDTO> problems
) {
}
