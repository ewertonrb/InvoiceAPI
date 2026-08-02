package com.invoice.invoice_api.dto.companyInvitation;

import com.invoice.invoice_api.enums.CompanyRole;

import java.time.LocalDateTime;

public record CompanyInvitationPublicResponseDTO(
        String companyName,

        String invitedName,

        String invitedSurname,

        String email,

        CompanyRole role,

        LocalDateTime expiresAt,

        boolean valid
) {
}
