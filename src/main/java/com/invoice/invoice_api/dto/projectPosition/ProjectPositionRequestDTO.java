package com.invoice.invoice_api.dto.projectPosition;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

    public record ProjectPositionRequestDTO(

            @NotNull(message = "Project ID is required")
            Long projectId,

            @NotBlank(message = "Position name is required")
            @Size(
                    max = 100,
                    message = "Position name must contain at most 100 characters"
            )

            String positionName
    ) {

    }

