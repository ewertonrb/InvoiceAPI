package com.invoice.invoice_api.dto.invoice;

import java.time.LocalDate;
import java.util.List;

public record GenerateInvoiceDraftsResponseDTO(
        LocalDate periodStart,

        LocalDate periodEnd,

        Integer generatedCount,

        Integer skippedCount,

        List<InvoiceResponseDTO> invoices,

        List<SkippedInvoiceWorkerDTO> skippedWorkers
) {
}
