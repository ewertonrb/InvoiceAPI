package com.invoice.invoice_api.dto.joinLink;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;

public record AcceptCompanyJoinLinkResponseDTO(
        Long appUserId,

        String name,

        String surname,

        String email,

        Long companyId,

        String companyName,

        Long membershipId,

        CompanyRole role,

        MembershipStatus membershipStatus,

        Integer remainingUses,

        boolean newAccountCreated,

        String nextStep
) {
}
