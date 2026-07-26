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
                membership.getAppUser().getName(),
                membership.getAppUser().getEmail(),

                membership.getCompany().getId(),
                membership.getCompany().getName(),

                membership.getRole(),
                membership.getActive(),

                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }
}
