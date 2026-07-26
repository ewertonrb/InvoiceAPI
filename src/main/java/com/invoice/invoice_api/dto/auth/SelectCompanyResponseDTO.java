package com.invoice.invoice_api.dto.auth;

import com.invoice.invoice_api.enums.CompanyRole;

public record SelectCompanyResponseDTO(
        String token,
        String tokenType,
        Long expiresIn,

        Long userId,
        String name,
        String email,

        Long companyId,
        String companyName,
        CompanyRole role
) {
}
