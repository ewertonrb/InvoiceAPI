package com.invoice.invoice_api.dto.joinLink;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.JoinLinkStatus;

import java.time.LocalDateTime;

public record CompanyJoinLinkResponseDTO(
        Long id,

        Long companyId,

        String companyName,

        CompanyRole role,

        JoinLinkStatus status,

        Integer maxUses,

        Integer currentUses,

        Integer remainingUses,

        LocalDateTime expiresAt,

        LocalDateTime disabledAt,

        Long createdByUserId,

        String createdByName,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
