package com.invoice.invoice_api.dto.companyInvitation;

import jakarta.validation.constraints.NotBlank;

public record DeclineCompanyInvitationRequestDTO(

        @NotBlank(message = "Invitation token is required.")
        String token

) {
}
