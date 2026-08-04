package com.invoice.invoice_api.dto.workLog;

import java.time.LocalTime;

public record WorkLogTimeResponseDTO(
        LocalTime startTime,

        LocalTime finishTime,

        Integer unpaidBreakMinutes,

        Long workedMinutes,

        Boolean crossesMidnight
) {
}
