package com.invoice.invoice_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDTO(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String confirmPassword
) {}
