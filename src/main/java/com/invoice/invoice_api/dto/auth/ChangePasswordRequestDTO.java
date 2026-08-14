package com.invoice.invoice_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {}
