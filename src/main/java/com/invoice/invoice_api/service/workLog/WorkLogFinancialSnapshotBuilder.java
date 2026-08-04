package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTravel;
import com.invoice.invoice_api.repository.ProjectRoleRateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Component
public class WorkLogFinancialSnapshotBuilder {
    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    /*
     * Keep this configurable when environment-specific
     * tax settings are introduced.
     */
    private static final BigDecimal GST_RATE =
            new BigDecimal("0.10");

    private final ProjectRoleRateRepository rateRepository;

    public WorkLogFinancialSnapshotBuilder(
            ProjectRoleRateRepository rateRepository
    ) {
        this.rateRepository = rateRepository;
    }

    public WorkLogFinancialSnapshot build(
            WorkLog workLog
    ) {
        ProjectPosition projectPosition =
                workLog.getProjectPosition();

        Project project =
                projectPosition.getProject();

        Company company =
                project.getCompany();

        WorkerProfile workerProfile =
                workLog.getWorkerProfile();

        AppUser appUser =
                workerProfile.getAppUser();

        ProjectRoleRate roleRate =
                rateRepository
                        .findActiveRateForDate(
                                projectPosition.getId(),
                                workLog.getWorkDate()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "No active rate was found for project position "
                                                + projectPosition.getId()
                                                + " on "
                                                + workLog.getWorkDate()
                                                + "."
                                )
                        );

        Map<RateType, ProjectRoleRateItem> items =
                mapActiveItems(roleRate);

        ProjectRoleRateItem regularItem =
                requireItem(
                        items,
                        RateType.REGULAR,
                        workLog.getRegularHours()
                );

        BigDecimal baseRate =
                resolveRegularBaseRate(regularItem);

        RateResult regular =
                resolveRateAndAmount(
                        items,
                        RateType.REGULAR,
                        workLog.getRegularHours(),
                        baseRate
                );

        RateResult overtime15 =
                resolveRateAndAmount(
                        items,
                        RateType.OVERTIME_1_5,
                        workLog.getOvertime15Hours(),
                        baseRate
                );

        RateResult overtime20 =
                resolveRateAndAmount(
                        items,
                        RateType.OVERTIME_2_0,
                        workLog.getOvertime20Hours(),
                        baseRate
                );

        RateResult saturday =
                resolveRateAndAmount(
                        items,
                        RateType.SATURDAY,
                        workLog.getSaturdayHours(),
                        baseRate
                );

        RateResult sunday =
                resolveRateAndAmount(
                        items,
                        RateType.SUNDAY,
                        workLog.getSundayHours(),
                        baseRate
                );

        RateResult publicHoliday =
                resolveRateAndAmount(
                        items,
                        RateType.PUBLIC_HOLIDAY,
                        workLog.getPublicHolidayHours(),
                        baseRate
                );

        WorkLogTravel travel =
                workLog.getTravel();

        RateResult travelTime =
                resolveRateAndAmount(
                        items,
                        RateType.TRAVEL_TIME,
                        travel.getTravelHours(),
                        baseRate
                );

        RateResult kilometre =
                resolveRateAndAmount(
                        items,
                        RateType.KILOMETRE,
                        travel.getKilometres(),
                        baseRate
                );

        RateResult lafha =
                resolveRateAndAmount(
                        items,
                        RateType.LAFHA,
                        BigDecimal.valueOf(
                                travel.getLafhaNights()
                        ),
                        baseRate
                );

        BigDecimal subtotal =
                money(
                        regular.amount()
                                .add(overtime15.amount())
                                .add(overtime20.amount())
                                .add(saturday.amount())
                                .add(sunday.amount())
                                .add(publicHoliday.amount())
                                .add(travelTime.amount())
                                .add(kilometre.amount())
                                .add(lafha.amount())
                );

        boolean gstRegistered =
                Boolean.TRUE.equals(
                        workerProfile.getGstRegistered()
                );

        BigDecimal gstAmount =
                gstRegistered
                        ? money(
                        subtotal.multiply(GST_RATE)
                )
                        : money(ZERO);

        BigDecimal totalAmount =
                money(
                        subtotal.add(gstAmount)
                );

        WorkLogFinancialSnapshot snapshot =
                new WorkLogFinancialSnapshot();

        /*
         * Historical text data
         */
        snapshot.setCompanyName(
                company.getName()
        );

        snapshot.setProjectName(
                project.getName()
        );

        snapshot.setPositionName(
                projectPosition.getPositionName()
        );

        snapshot.setWorkerName(
                appUser.getFullName()
        );

        snapshot.setWorkerAbn(
                workerProfile.getAbn()
        );

        snapshot.setWorkerGstRegistered(
                gstRegistered
        );

        /*
         * Historical rates
         */
        snapshot.setRegularRate(
                regular.rate()
        );

        snapshot.setOvertime15Rate(
                overtime15.rate()
        );

        snapshot.setOvertime20Rate(
                overtime20.rate()
        );

        snapshot.setSaturdayRate(
                saturday.rate()
        );

        snapshot.setSundayRate(
                sunday.rate()
        );

        snapshot.setPublicHolidayRate(
                publicHoliday.rate()
        );

        snapshot.setTravelRate(
                travelTime.rate()
        );

        snapshot.setKilometreRate(
                kilometre.rate()
        );

        snapshot.setLafhaRate(
                lafha.rate()
        );

        /*
         * Calculated historical amounts
         */
        snapshot.setRegularAmount(
                regular.amount()
        );

        snapshot.setOvertime15Amount(
                overtime15.amount()
        );

        snapshot.setOvertime20Amount(
                overtime20.amount()
        );

        snapshot.setSaturdayAmount(
                saturday.amount()
        );

        snapshot.setSundayAmount(
                sunday.amount()
        );

        snapshot.setPublicHolidayAmount(
                publicHoliday.amount()
        );

        snapshot.setTravelAmount(
                travelTime.amount()
        );

        snapshot.setKilometreAmount(
                kilometre.amount()
        );

        snapshot.setLafhaAmount(
                lafha.amount()
        );

        snapshot.setSubtotalAmount(
                subtotal
        );

        snapshot.setGstAmount(
                gstAmount
        );

        snapshot.setTotalAmount(
                totalAmount
        );

        return snapshot;
    }

    /*
     * ============================================================
     * RATE RESOLUTION
     * ============================================================
     */

    private RateResult resolveRateAndAmount(
            Map<RateType, ProjectRoleRateItem> items,
            RateType rateType,
            BigDecimal quantity,
            BigDecimal baseRate
    ) {
        BigDecimal normalizedQuantity =
                zeroIfNull(quantity);

        ProjectRoleRateItem item =
                requireItem(
                        items,
                        rateType,
                        normalizedQuantity
                );

        /*
         * A missing rate is acceptable only when the corresponding
         * quantity is zero.
         */
        if (item == null) {
            return new RateResult(
                    rate(ZERO),
                    money(ZERO)
            );
        }

        BigDecimal effectiveRate =
                resolveEffectiveRate(
                        item,
                        baseRate
                );

        BigDecimal amount;

        if (
                item.getCalculationType()
                        == RateCalculationType.FIXED_AMOUNT
        ) {
            amount =
                    isPositive(normalizedQuantity)
                            ? item.getValue()
                            : ZERO;
        } else {
            amount =
                    normalizedQuantity.multiply(
                            effectiveRate
                    );
        }

        return new RateResult(
                rate(effectiveRate),
                money(amount)
        );
    }

    private BigDecimal resolveEffectiveRate(
            ProjectRoleRateItem item,
            BigDecimal baseRate
    ) {
        BigDecimal value =
                zeroIfNull(item.getValue());

        return switch (
                item.getCalculationType()
                ) {
            case BASE_RATE ->
                    baseRate;

            case MULTIPLIER ->
                    baseRate.multiply(value);

            case FIXED_RATE,
                 FIXED_AMOUNT ->
                    value;
        };
    }

    private BigDecimal resolveRegularBaseRate(
            ProjectRoleRateItem regularItem
    ) {
        if (regularItem == null) {
            throw new BusinessException(
                    "The REGULAR rate is required."
            );
        }

        if (
                regularItem.getCalculationType()
                        == RateCalculationType.MULTIPLIER
        ) {
            throw new BusinessException(
                    "The REGULAR rate cannot use MULTIPLIER calculation."
            );
        }

        if (
                regularItem.getCalculationType()
                        == RateCalculationType.FIXED_AMOUNT
        ) {
            throw new BusinessException(
                    "The REGULAR rate cannot use FIXED_AMOUNT calculation."
            );
        }

        BigDecimal value =
                regularItem.getValue();

        if (
                value == null
                        || value.compareTo(ZERO) < 0
        ) {
            throw new BusinessException(
                    "The REGULAR rate must contain a valid non-negative value."
            );
        }

        return value;
    }

    /*
     * ============================================================
     * RATE ITEM LOOKUP
     * ============================================================
     */

    private Map<RateType, ProjectRoleRateItem> mapActiveItems(
            ProjectRoleRate roleRate
    ) {
        Map<RateType, ProjectRoleRateItem> items =
                new EnumMap<>(RateType.class);

        for (
                ProjectRoleRateItem item
                : roleRate.getItems()
        ) {
            if (
                    Boolean.TRUE.equals(
                            item.getActive()
                    )
            ) {
                items.put(
                        item.getRateType(),
                        item
                );
            }
        }

        return items;
    }

    private ProjectRoleRateItem requireItem(
            Map<RateType, ProjectRoleRateItem> items,
            RateType rateType,
            BigDecimal quantity
    ) {
        ProjectRoleRateItem item =
                items.get(rateType);

        if (
                item == null
                        && isPositive(quantity)
        ) {
            throw new BusinessException(
                    "Rate item "
                            + rateType
                            + " is required because its work quantity is greater than zero."
            );
        }

        return item;
    }

    /*
     * ============================================================
     * NUMERIC HELPERS
     * ============================================================
     */

    private BigDecimal zeroIfNull(
            BigDecimal value
    ) {
        return value == null
                ? ZERO
                : value;
    }

    private boolean isPositive(
            BigDecimal value
    ) {
        return value != null
                && value.compareTo(ZERO) > 0;
    }

    private BigDecimal rate(
            BigDecimal value
    ) {
        return zeroIfNull(value)
                .setScale(
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal money(
            BigDecimal value
    ) {
        return zeroIfNull(value)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private record RateResult(
            BigDecimal rate,
            BigDecimal amount
    ) {
    }
}
