package com.invoice.invoice_api.dto.joinLink;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPublicJoinLinkRequestDTO(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Size(min = 2, max = 100) String surname,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String token
) {}
