package com.invoice.invoice_api.dto.workLog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WorkLogTravelRequestDTO(
        @DecimalMin(
                value = "0.00",
                message = "Travel hours cannot be negative"
        )
        BigDecimal travelHours,

        @DecimalMin(
                value = "0.00",
                message = "Kilometres cannot be negative"
        )
        BigDecimal kilometres,

        @PositiveOrZero(
                message = "LAFHA nights cannot be negative"
        )
        Integer lafhaNights

) {
}
