package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.companyInvitation.CompanyInvitationResponseDTO;
import com.invoice.invoice_api.model.CompanyInvitation;

public final class CompanyInvitationMapper {

    private CompanyInvitationMapper() {
    }

    public static CompanyInvitationResponseDTO toResponseDTO(
            CompanyInvitation invitation
    ) {
        return new CompanyInvitationResponseDTO(
                invitation.getId(),
                invitation.getCompany().getId(),
                invitation.getCompany().getName(),
                invitation.getName(),
                invitation.getSurname(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getInvitedBy().getId(),
                invitation.getInvitedBy().getFullName(),
                invitation.getExpiresAt(),
                invitation.getAcceptedAt(),
                invitation.getCancelledAt(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt()
        );
    }
}