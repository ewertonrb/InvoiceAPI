package com.invoice.invoice_api.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequestDTO(
        @NotBlank(message = "Company name is required")
        @Size(max = 150, message = "Company name must have at most 150 characters")
        String name,

        @NotBlank(message = "ABN is required")
        @Size(max = 20, message = "ABN must have at most 20 characters")
        String abn,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must have at most 150 characters")
        String email,

        @Size(max = 30, message = "Phone must have at most 30 characters")
        String phone,

        @Size(max = 255, message = "Address must have at most 255 characters")
        String address,

        Boolean contractorInvoiceGstEnabled,

        Boolean active
) {
}
