package com.invoice.invoice_api.dto.companyInvitation;

public record CompanyInvitationCreatedResponseDTO(
        CompanyInvitationResponseDTO invitation,

        String invitationUrl
) {
}
