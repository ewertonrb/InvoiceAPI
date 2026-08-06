package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkResponseDTO;
import com.invoice.invoice_api.model.CompanyJoinLink;

public final class CompanyJoinLinkMapper {
    private CompanyJoinLinkMapper() {
    }

    public static CompanyJoinLinkResponseDTO toResponseDTO(
            CompanyJoinLink joinLink
    ) {
        return new CompanyJoinLinkResponseDTO(
                joinLink.getId(),
                joinLink.getCompany().getId(),
                joinLink.getCompany().getName(),
                joinLink.getRole(),
                joinLink.getStatus(),
                joinLink.getMaxUses(),
                joinLink.getCurrentUses(),
                calculateRemainingUses(joinLink),
                joinLink.getExpiresAt(),
                joinLink.getDisabledAt(),
                joinLink.getCreatedBy().getId(),
                joinLink.getCreatedBy().getFullName(),
                joinLink.getCreatedAt(),
                joinLink.getUpdatedAt()
        );
    }

    private static int calculateRemainingUses(
            CompanyJoinLink joinLink
    ) {
        return Math.max(
                joinLink.getMaxUses()
                        - joinLink.getCurrentUses(),
                0
        );
    }
}
