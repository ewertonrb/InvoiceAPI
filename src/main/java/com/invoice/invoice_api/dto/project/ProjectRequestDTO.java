package com.invoice.invoice_api.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequestDTO(

        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must have at most 100 characters")
        String name
) {
}
