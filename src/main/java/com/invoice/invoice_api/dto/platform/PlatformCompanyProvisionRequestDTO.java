package com.invoice.invoice_api.dto.platform;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PlatformCompanyProvisionRequestDTO(
        @NotNull @Valid CompanyRequestDTO company,
        @NotNull @Valid PlatformOwnerRequestDTO owner
) {
}
