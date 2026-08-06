package com.invoice.invoice_api.dto.platform;

import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;

public record PlatformCompanyProvisionResponseDTO(
        CompanyResponseDTO company,
        CompanyMembershipResponseDTO ownerMembership,
        boolean ownerCreated
) {
}
