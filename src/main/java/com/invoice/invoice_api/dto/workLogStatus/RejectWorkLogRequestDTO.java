package com.invoice.invoice_api.dto.workLogStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectWorkLogRequestDTO(
        @NotBlank(message = "Rejection reason is required.")
        @Size(max = 500)
        String rejectionReason
) {
}
