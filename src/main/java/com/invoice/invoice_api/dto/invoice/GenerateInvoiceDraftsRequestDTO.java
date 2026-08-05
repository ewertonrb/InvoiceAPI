package com.invoice.invoice_api.dto.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record GenerateInvoiceDraftsRequestDTO(
        @NotNull(
                message = "Period start is required"
        )
        LocalDate periodStart,

        @NotNull(
                message = "Period end is required"
        )
        LocalDate periodEnd,

        /*
         * When null or empty, drafts are generated for every
         * eligible worker in the selected period.
         */
        @Size(
                max = 500,
                message = "No more than 500 workers can be selected at once"
        )
        List<Long> workerProfileIds
) {
}
