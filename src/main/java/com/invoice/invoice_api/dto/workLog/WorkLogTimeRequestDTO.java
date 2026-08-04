package com.invoice.invoice_api.dto.workLog;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalTime;

public record WorkLogTimeRequestDTO (
        LocalTime startTime,

        LocalTime finishTime,

        @PositiveOrZero(
                message = "Unpaid break minutes cannot be negative"
        )
        Integer unpaidBreakMinutes
){
}
