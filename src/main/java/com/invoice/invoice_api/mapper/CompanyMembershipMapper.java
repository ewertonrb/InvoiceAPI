package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;
import com.invoice.invoice_api.model.CompanyMembership;

public class CompanyMembershipMapper {
    private CompanyMembershipMapper() {
    }

    public static CompanyMembershipResponseDTO toResponseDTO(
            CompanyMembership membership
    ) {
        return new CompanyMembershipResponseDTO(
                membership.getId(),
                membership.getAppUser().getId(),
                membership.getCompany().getId(),
                membership.getRole(),
                membership.getStatus(),
                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }
}
