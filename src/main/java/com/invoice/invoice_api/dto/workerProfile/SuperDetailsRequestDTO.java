package com.invoice.invoice_api.dto.workerProfile;

import jakarta.validation.constraints.Size;

public record SuperDetailsRequestDTO(
        @Size(max = 150, message = "Fund name must contain at most 150 characters")
        String fundName,

        @Size(max = 50, message = "USI must contain at most 50 characters")
        String usi,

        @Size(max = 100, message = "Member number must contain at most 100 characters")
        String memberNumber
) {
}
