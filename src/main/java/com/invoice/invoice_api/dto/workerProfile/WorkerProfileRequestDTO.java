package com.invoice.invoice_api.dto.workerProfile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WorkerProfileRequestDTO(
        @Pattern(
                regexp = "^(?:\\d[\\s-]?){11}$",
                message = "ABN must contain 11 digits"
        )
        String abn,

        Boolean gstRegistered,

        @Size(
                max = 30,
                message = "Phone must contain at most 30 characters"
        )
        String phone,

        @Valid
        BankDetailsRequestDTO bankDetails,

        @Valid
        SuperDetailsRequestDTO superDetails,

        @Size(
                max = 1000,
                message = "Notes must contain at most 1000 characters"
        )
        String notes

) {
}
