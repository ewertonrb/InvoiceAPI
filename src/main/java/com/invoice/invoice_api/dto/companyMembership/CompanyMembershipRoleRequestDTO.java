package com.invoice.invoice_api.dto.companyMembership;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.validation.constraints.NotNull;

public record CompanyMembershipRoleRequestDTO(
        @NotNull(message = "Company role is required")
        CompanyRole role

) {
}
