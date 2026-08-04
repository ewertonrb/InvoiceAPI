package com.invoice.invoice_api.dto.workLog;

import java.math.BigDecimal;

public record WorkLogTravelResponseDTO(
        BigDecimal travelHours,

        BigDecimal kilometres,

        Integer lafhaNights
) {
}
