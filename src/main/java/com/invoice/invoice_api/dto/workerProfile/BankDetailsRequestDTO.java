package com.invoice.invoice_api.dto.workerProfile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BankDetailsRequestDTO(

        @Size(max = 100, message = "Bank name must contain at most 100 characters")
        String bankName,

        @Size(max = 150, message = "Account name must contain at most 150 characters")
        String accountName,

        @Pattern(
                regexp = "^\\d{3}-?\\d{3}$",
                message = "BSB must contain exactly 6 digits"
        )
        String bsb,

        @Pattern(
                regexp = "^\\d{4,20}$",
                message = "Account number must contain between 4 and 20 digits"
        )
        String accountNumber

) {
}
