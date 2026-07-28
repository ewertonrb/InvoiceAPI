package com.invoice.invoice_api.dto.companyInvitation;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.InvitationStatus;

import java.time.LocalDateTime;

public record CompanyInvitationResponseDTO(

        Long id,

        Long companyId,

        String companyName,

        String name,

        String surname,

        String email,

        CompanyRole role,

        InvitationStatus status,

        Long invitedByUserId,

        String invitedByName,

        LocalDateTime expiresAt,

        LocalDateTime acceptedAt,

        LocalDateTime cancelledAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}