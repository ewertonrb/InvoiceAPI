package com.invoice.invoice_api.dto.invoice;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IssueInvoiceRequestDTO(

        @NotNull(message = "Issue date is required")
        LocalDate issueDate,

        @NotNull(message = "Due date is required")
        LocalDate dueDate

) {
}
