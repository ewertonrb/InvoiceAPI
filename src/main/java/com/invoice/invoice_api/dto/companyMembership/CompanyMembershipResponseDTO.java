package com.invoice.invoice_api.dto.companyMembership;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;

import java.time.LocalDateTime;

public record CompanyMembershipResponseDTO(
        Long id,

        Long appUserId,

        Long companyId,

        CompanyRole role,

        MembershipStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
