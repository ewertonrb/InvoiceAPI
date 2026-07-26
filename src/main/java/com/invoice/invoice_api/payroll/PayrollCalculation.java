package com.invoice.invoice_api.payroll;

import java.math.BigDecimal;

public record PayrollCalculation(
        BigDecimal regularAmount,

        BigDecimal overtime15Amount,

        BigDecimal overtime20Amount,

        BigDecimal saturdayAmount,

        BigDecimal sundayAmount,

        BigDecimal publicHolidayAmount,

        BigDecimal travelAmount,

        BigDecimal kilometreAmount,

        BigDecimal lafhaAmount,

        BigDecimal total
) {
}
