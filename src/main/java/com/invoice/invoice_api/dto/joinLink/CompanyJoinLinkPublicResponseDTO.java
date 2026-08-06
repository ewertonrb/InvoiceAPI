package com.invoice.invoice_api.dto.joinLink;

import com.invoice.invoice_api.enums.CompanyRole;

import java.time.LocalDateTime;

public record CompanyJoinLinkPublicResponseDTO(
        String companyName,

        CompanyRole role,

        int remainingUses,

        LocalDateTime expiresAt,

        boolean valid
) {
}
