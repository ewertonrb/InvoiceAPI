package com.invoice.invoice_api.dto.auth;

import jakarta.validation.constraints.NotNull;

public record SelectCompanyRequestDTO(
        @NotNull(message = "Company ID is required")
        Long companyId
) {
}
