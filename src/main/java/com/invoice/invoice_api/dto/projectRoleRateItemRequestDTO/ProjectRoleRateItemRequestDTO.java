package com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO;

import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProjectRoleRateItemRequestDTO(
        @NotNull(message = "Rate type is required")
        RateType rateType,

        @NotNull(message = "Calculation type is required")
        RateCalculationType calculationType,

        @NotNull(message = "Rate value is required")
        @DecimalMin(
                value = "0.0001",
                message = "Rate value must be greater than zero"
        )
        BigDecimal value,

        @Size(
                max = 200,
                message = "Description must contain at most 200 characters"
        )
        String description

) {
}
