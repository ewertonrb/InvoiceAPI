package com.invoice.invoice_api.dto.workLog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkLogRequestDTO(
        @NotNull(message = "Worker profile ID is required")
        Long workerProfileId,

        @NotNull(
                message = "Project position ID is required"
        )
        Long projectPositionId,

        @NotNull(message = "Work date is required")
        LocalDate workDate,

        @Valid
        WorkLogTimeRequestDTO workTime,

        @NotNull(message = "Regular hours are required")
        @DecimalMin(
                value = "0.00",
                message = "Regular hours cannot be negative"
        )
        BigDecimal regularHours,

        @NotNull(
                message = "Overtime 1.5 hours are required"
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "Overtime 1.5 hours cannot be negative"
        )
        BigDecimal overtime15Hours,

        @NotNull(
                message = "Overtime 2.0 hours are required"
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "Overtime 2.0 hours cannot be negative"
        )
        BigDecimal overtime20Hours,

        @NotNull(message = "Saturday hours are required")
        @DecimalMin(
                value = "0.00",
                message = "Saturday hours cannot be negative"
        )
        BigDecimal saturdayHours,

        @NotNull(message = "Sunday hours are required")
        @DecimalMin(
                value = "0.00",
                message = "Sunday hours cannot be negative"
        )
        BigDecimal sundayHours,

        @NotNull(
                message = "Public holiday hours are required"
        )
        @DecimalMin(
                value = "0.00",
                message =
                        "Public holiday hours cannot be negative"
        )
        BigDecimal publicHolidayHours,

        @Valid
        WorkLogTravelRequestDTO travel,

        @Size(
                max = 500,
                message =
                        "Notes must contain at most 500 characters"
        )
        String notes

) {
}
