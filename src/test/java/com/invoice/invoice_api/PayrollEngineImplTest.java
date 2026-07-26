package com.invoice.invoice_api;

import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import com.invoice.invoice_api.model.ProjectRoleRate;
import com.invoice.invoice_api.model.ProjectRoleRateItem;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.payroll.PayrollCalculation;
import com.invoice.invoice_api.payroll.PayrollEngine;
import com.invoice.invoice_api.payroll.PayrollEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayrollEngineImplTest {
    private PayrollEngine payrollEngine;

    @BeforeEach
    void setUp() {
        payrollEngine = new PayrollEngineImpl();
    }

    @Test
    void shouldCalculatePayrollSuccessfully() {

        WorkLog workLog = createWorkLog();
        ProjectRoleRate rate = createProjectRoleRate();

        PayrollCalculation result =
                payrollEngine.calculate(
                        workLog,
                        rate
                );

        assertEquals(
                new BigDecimal("400.00"),
                result.regularAmount()
        );

        assertEquals(
                new BigDecimal("150.00"),
                result.overtime15Amount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.overtime20Amount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.saturdayAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.sundayAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.publicHolidayAmount()
        );

        assertEquals(
                new BigDecimal("50.00"),
                result.travelAmount()
        );

        assertEquals(
                new BigDecimal("23.75"),
                result.kilometreAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.lafhaAmount()
        );

        assertEquals(
                new BigDecimal("623.75"),
                result.total()
        );
    }

    private WorkLog createWorkLog() {

        WorkLog workLog = new WorkLog();

        workLog.setRegularHours(
                new BigDecimal("8.00")
        );

        workLog.setOvertime15Hours(
                new BigDecimal("2.00")
        );

        workLog.setOvertime20Hours(
                BigDecimal.ZERO
        );

        workLog.setSaturdayHours(
                BigDecimal.ZERO
        );

        workLog.setSundayHours(
                BigDecimal.ZERO
        );

        workLog.setPublicHolidayHours(
                BigDecimal.ZERO
        );

        workLog.setTravelHours(
                new BigDecimal("1.00")
        );

        workLog.setKilometres(
                new BigDecimal("25.00")
        );

        workLog.setLafhaNights(0);

        return workLog;
    }

    private ProjectRoleRate createProjectRoleRate() {

        ProjectRoleRate rate =
                new ProjectRoleRate();

        List<ProjectRoleRateItem> items =
                new ArrayList<>();

        items.add(
                createRateItem(
                        RateType.REGULAR,
                        RateCalculationType.BASE_RATE,
                        "50.00"
                )
        );

        items.add(
                createRateItem(
                        RateType.OVERTIME_1_5,
                        RateCalculationType.MULTIPLIER,
                        "1.50"
                )
        );

        items.add(
                createRateItem(
                        RateType.OVERTIME_2_0,
                        RateCalculationType.MULTIPLIER,
                        "2.00"
                )
        );

        items.add(
                createRateItem(
                        RateType.SATURDAY,
                        RateCalculationType.MULTIPLIER,
                        "1.50"
                )
        );

        items.add(
                createRateItem(
                        RateType.SUNDAY,
                        RateCalculationType.MULTIPLIER,
                        "2.00"
                )
        );

        items.add(
                createRateItem(
                        RateType.PUBLIC_HOLIDAY,
                        RateCalculationType.MULTIPLIER,
                        "2.50"
                )
        );

        items.add(
                createRateItem(
                        RateType.TRAVEL_TIME,
                        RateCalculationType.MULTIPLIER,
                        "1.00"
                )
        );

        items.add(
                createRateItem(
                        RateType.KILOMETRE,
                        RateCalculationType.FIXED_RATE,
                        "0.95"
                )
        );

        items.add(
                createRateItem(
                        RateType.LAFHA,
                        RateCalculationType.FIXED_AMOUNT,
                        "120.00"
                )
        );

        rate.setItems(items);

        return rate;
    }

    private ProjectRoleRateItem createRateItem(
            RateType rateType,
            RateCalculationType calculationType,
            String value
    ) {

        ProjectRoleRateItem item =
                new ProjectRoleRateItem();

        item.setRateType(rateType);
        item.setCalculationType(calculationType);
        item.setValue(new BigDecimal(value));
        item.setActive(true);

        return item;
    }
}
