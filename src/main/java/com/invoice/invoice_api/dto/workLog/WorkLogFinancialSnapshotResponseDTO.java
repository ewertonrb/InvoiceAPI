package com.invoice.invoice_api.dto.workLog;

import java.math.BigDecimal;

public record WorkLogFinancialSnapshotResponseDTO(
        String companyName,

        String projectName,

        String positionName,

        String workerName,

        String workerAbn,

        Boolean workerGstRegistered,

        BigDecimal regularRate,

        BigDecimal overtime15Rate,

        BigDecimal overtime20Rate,

        BigDecimal saturdayRate,

        BigDecimal sundayRate,

        BigDecimal publicHolidayRate,

        BigDecimal travelRate,

        BigDecimal kilometreRate,

        BigDecimal lafhaRate,

        BigDecimal regularAmount,

        BigDecimal overtime15Amount,

        BigDecimal overtime20Amount,

        BigDecimal saturdayAmount,

        BigDecimal sundayAmount,

        BigDecimal publicHolidayAmount,

        BigDecimal travelAmount,

        BigDecimal kilometreAmount,

        BigDecimal lafhaAmount,

        BigDecimal subtotalAmount,

        BigDecimal gstAmount,

        BigDecimal totalAmount
) {
}
