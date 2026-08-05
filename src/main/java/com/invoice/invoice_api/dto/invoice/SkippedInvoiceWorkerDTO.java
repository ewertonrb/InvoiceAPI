package com.invoice.invoice_api.dto.invoice;

import java.util.List;

public record SkippedInvoiceWorkerDTO(
        Long workerProfileId,

        String workerName,

        List<String> reasons
) {
}
