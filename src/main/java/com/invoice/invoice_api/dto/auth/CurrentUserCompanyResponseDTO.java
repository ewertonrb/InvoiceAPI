package com.invoice.invoice_api.dto.auth;

import com.invoice.invoice_api.enums.CompanyRole;

public record CurrentUserCompanyResponseDTO(
        Long membershipId,
        Long companyId,
        String companyName,
        CompanyRole role
) {
}
