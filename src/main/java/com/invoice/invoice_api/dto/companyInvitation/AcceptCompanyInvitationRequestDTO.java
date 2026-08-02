package com.invoice.invoice_api.dto.companyInvitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptCompanyInvitationRequestDTO(
        @NotBlank(message = "Invitation token is required.")
        String token,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters."
        )
        String password,

        @NotBlank(message = "Password confirmation is required.")
        String confirmPassword
) {
}
