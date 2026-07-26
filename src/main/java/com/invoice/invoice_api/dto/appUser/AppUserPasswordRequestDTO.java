package com.invoice.invoice_api.dto.appUser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppUserPasswordRequestDTO(
        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters"
        )
        String password
) {
}
