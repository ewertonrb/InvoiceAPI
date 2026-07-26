package com.invoice.invoice_api.dto.companyMembership;

import com.invoice.invoice_api.enums.CompanyRole;

import java.time.LocalDateTime;

public record CompanyMembershipResponseDTO(
        Long id,

        Long appUserId,
        String appUserName,
        String appUserEmail,

        Long companyId,
        String companyName,

        CompanyRole role,
        Boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
