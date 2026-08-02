package com.invoice.invoice_api.dto.joinLink;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptCompanyJoinLinkRequestDTO(
        @NotBlank(message = "Join link token is required.")
        String token,

        @NotBlank(message = "Name is required.")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Surname is required.")
        @Size(max = 100)
        String surname,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters."
        )
        String password,

        @NotBlank(message = "Password confirmation is required.")
        String confirmPassword
) {
}
