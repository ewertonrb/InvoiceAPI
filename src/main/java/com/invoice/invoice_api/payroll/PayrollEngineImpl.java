package com.invoice.invoice_api.payroll;

import com.invoice.invoice_api.enums.RateType;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.ProjectRoleRate;
import com.invoice.invoice_api.model.ProjectRoleRateItem;
import com.invoice.invoice_api.model.WorkLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PayrollEngineImpl implements PayrollEngine{

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private static final int MONEY_SCALE = 2;

    @Override
    public PayrollCalculation calculate(
            WorkLog workLog,
            ProjectRoleRate rate
    ) {
        validateInputs(workLog, rate);

        BigDecimal baseRate =
                findRateValue(
                        rate,
                        RateType.REGULAR
                );

        BigDecimal regularAmount =
                calculateAmount(
                        workLog.getRegularHours(),
                        RateType.REGULAR,
                        rate,
                        baseRate
                );

        BigDecimal overtime15Amount =
                calculateAmount(
                        workLog.getOvertime15Hours(),
                        RateType.OVERTIME_1_5,
                        rate,
                        baseRate
                );

        BigDecimal overtime20Amount =
                calculateAmount(
                        workLog.getOvertime20Hours(),
                        RateType.OVERTIME_2_0,
                        rate,
                        baseRate
                );

        BigDecimal saturdayAmount =
                calculateAmount(
                        workLog.getSaturdayHours(),
                        RateType.SATURDAY,
                        rate,
                        baseRate
                );

        BigDecimal sundayAmount =
                calculateAmount(
                        workLog.getSundayHours(),
                        RateType.SUNDAY,
                        rate,
                        baseRate
                );

        BigDecimal publicHolidayAmount =
                calculateAmount(
                        workLog.getPublicHolidayHours(),
                        RateType.PUBLIC_HOLIDAY,
                        rate,
                        baseRate
                );

        BigDecimal travelAmount =
                calculateAmount(
                        workLog.getTravelHours(),
                        RateType.TRAVEL_TIME,
                        rate,
                        baseRate
                );

        BigDecimal kilometreAmount =
                calculateAmount(
                        workLog.getKilometres(),
                        RateType.KILOMETRE,
                        rate,
                        baseRate
                );

        BigDecimal lafhaAmount =
                calculateAmount(
                        BigDecimal.valueOf(
                                workLog.getLafhaNights() == null
                                        ? 0
                                        : workLog.getLafhaNights()
                        ),
                        RateType.LAFHA,
                        rate,
                        baseRate
                );

        BigDecimal total =
                sum(
                        regularAmount,
                        overtime15Amount,
                        overtime20Amount,
                        saturdayAmount,
                        sundayAmount,
                        publicHolidayAmount,
                        travelAmount,
                        kilometreAmount,
                        lafhaAmount
                );

        return new PayrollCalculation(
                regularAmount,
                overtime15Amount,
                overtime20Amount,
                saturdayAmount,
                sundayAmount,
                publicHolidayAmount,
                travelAmount,
                kilometreAmount,
                lafhaAmount,
                total
        );
    }


    private BigDecimal calculateAmount(
            BigDecimal quantity,
            RateType rateType,
            ProjectRoleRate rate,
            BigDecimal baseRate
    ) {
        BigDecimal safeQuantity =
                normalizeQuantity(quantity);

        if (safeQuantity.signum() == 0) {
            return ZERO;
        }

        ProjectRoleRateItem rateItem =
                findRateItem(rate, rateType);

        if (rateItem.getCalculationType() == null) {
            throw new BusinessException(
                    "Calculation type is required for rate type "
                            + rateType
                            + "."
            );
        }

        BigDecimal rateValue =
                requireValidRateValue(rateItem);

        BigDecimal amount = switch (
                rateItem.getCalculationType()
                ) {
            case BASE_RATE ->
                    safeQuantity.multiply(rateValue);

            case MULTIPLIER ->
                    safeQuantity
                            .multiply(baseRate)
                            .multiply(rateValue);

            case FIXED_RATE ->
                    safeQuantity.multiply(rateValue);

            case FIXED_AMOUNT ->
                    safeQuantity.multiply(rateValue);
        };

        return money(amount);
    }

    private BigDecimal requireValidRateValue(
            ProjectRoleRateItem rateItem
    ) {
        if (rateItem.getValue() == null) {
            throw new BusinessException(
                    "Rate value is required for rate type "
                            + rateItem.getRateType()
                            + "."
            );
        }

        if (rateItem.getValue().signum() < 0) {
            throw new BusinessException(
                    "Rate value cannot be negative for rate type "
                            + rateItem.getRateType()
                            + "."
            );
        }

        return rateItem.getValue();
    }




    private ProjectRoleRateItem findRateItem(
            ProjectRoleRate rate,
            RateType rateType
    ) {

        return rate.getItems()
                .stream()
                .filter(Objects::nonNull)
                .filter(item ->
                        Boolean.TRUE.equals(item.getActive()))
                .filter(item ->
                        item.getRateType() == rateType)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(
                                "No active rate item was found for rate type "
                                        + rateType + "."
                        )
                );
    }

    private BigDecimal findRateValue(
            ProjectRoleRate rate,
            RateType rateType
    ) {
        return findRateItem(rate, rateType)
                .getValue();
    }


    private BigDecimal normalizeQuantity(
            BigDecimal quantity
    ) {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }

        if (quantity.signum() < 0) {
            throw new BusinessException(
                    "Payroll quantity cannot be negative."
            );
        }

        return quantity;
    }

    private BigDecimal sum(
            BigDecimal... values
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal value : values) {
            if (value != null) {
                total = total.add(value);
            }
        }

        return money(total);
    }

    private BigDecimal money(
            BigDecimal value
    ) {
        if (value == null) {
            return ZERO;
        }

        return value.setScale(
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void validateInputs(
            WorkLog workLog,
            ProjectRoleRate rate
    ) {
        if (workLog == null) {
            throw new BusinessException(
                    "WorkLog is required for payroll calculation."
            );
        }

        if (rate == null) {
            throw new BusinessException(
                    "Project role rate is required for payroll calculation."
            );
        }

        if (rate.getItems() == null
                || rate.getItems().isEmpty()) {

            throw new BusinessException(
                    "Project role rate must contain rate items."
            );
        }
    }
}
