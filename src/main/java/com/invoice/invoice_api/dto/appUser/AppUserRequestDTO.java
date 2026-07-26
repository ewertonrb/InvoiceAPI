package com.invoice.invoice_api.dto.appUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppUserRequestDTO(

        @NotBlank(message = "Name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Name must contain between 2 and 150 characters"
        )
        String name,

        @NotBlank(message = "Surname is required")
        @Size(
                min = 2,
                max = 100,
                message = "Name must contain between 2 and 150 characters"
        )
        String surname,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(
                max = 150,
                message = "Email must contain at most 150 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters"
        )
        String password

) {
}
