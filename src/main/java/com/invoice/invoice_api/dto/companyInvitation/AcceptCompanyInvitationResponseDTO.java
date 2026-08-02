package com.invoice.invoice_api.dto.companyInvitation;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;

public record AcceptCompanyInvitationResponseDTO(
        Long appUserId,

        String name,

        String surname,

        String email,

        Long companyId,

        String companyName,

        Long membershipId,

        CompanyRole role,

        MembershipStatus membershipStatus,

        boolean newAccountCreated
) {
}
