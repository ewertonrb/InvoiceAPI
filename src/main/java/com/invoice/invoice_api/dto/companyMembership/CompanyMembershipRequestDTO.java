package com.invoice.invoice_api.dto.companyMembership;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.validation.constraints.NotNull;

public record CompanyMembershipRequestDTO(
        @NotNull(message = "App user ID is required")
        Long appUserId,

        @NotNull(message = "Company ID is required")
        Long companyId,

        @NotNull(message = "Company role is required")
        CompanyRole role
) {
}
