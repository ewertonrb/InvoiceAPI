package com.invoice.invoice_api.dto.companyInvitation;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyInvitationRequestDTO(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        String surname,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotNull
        CompanyRole role
) {
}