package com.invoice.invoice_api.model.embeddable.workLog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class WorkLogFinancialSnapshot {
    /*
     * ============================================================
     * TEXT SNAPSHOT
     * ============================================================
     */

    @Column(
            name = "snapshot_company_name",
            length = 150
    )
    private String companyName;

    @Column(
            name = "snapshot_project_name",
            length = 150
    )
    private String projectName;

    @Column(
            name = "snapshot_position_name",
            length = 150
    )
    private String positionName;

    @Column(
            name = "snapshot_worker_name",
            length = 200
    )
    private String workerName;

    @Column(
            name = "snapshot_worker_abn",
            length = 11
    )
    private String workerAbn;

    @Column(
            name = "snapshot_worker_gst_registered"
    )
    private Boolean workerGstRegistered;

    @Column(
            name = "snapshot_gst_applied"
    )
    private Boolean gstApplied;

    /*
     * ============================================================
     * RATE SNAPSHOT
     * ============================================================
     */

    @Column(
            name = "snapshot_regular_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal regularRate;

    @Column(
            name = "snapshot_overtime_15_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal overtime15Rate;

    @Column(
            name = "snapshot_overtime_20_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal overtime20Rate;

    @Column(
            name = "snapshot_saturday_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal saturdayRate;

    @Column(
            name = "snapshot_sunday_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal sundayRate;

    @Column(
            name = "snapshot_public_holiday_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal publicHolidayRate;

    @Column(
            name = "snapshot_travel_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal travelRate;

    @Column(
            name = "snapshot_kilometre_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal kilometreRate;

    @Column(
            name = "snapshot_lafha_rate",
            precision = 12,
            scale = 2
    )
    private BigDecimal lafhaRate;

    /*
     * ============================================================
     * CALCULATED AMOUNTS
     * ============================================================
     */

    @Column(
            name = "snapshot_regular_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal regularAmount;

    @Column(
            name = "snapshot_overtime_15_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal overtime15Amount;

    @Column(
            name = "snapshot_overtime_20_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal overtime20Amount;

    @Column(
            name = "snapshot_saturday_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal saturdayAmount;

    @Column(
            name = "snapshot_sunday_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal sundayAmount;

    @Column(
            name = "snapshot_public_holiday_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal publicHolidayAmount;

    @Column(
            name = "snapshot_travel_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal travelAmount;

    @Column(
            name = "snapshot_kilometre_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal kilometreAmount;

    @Column(
            name = "snapshot_lafha_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal lafhaAmount;

    /*
     * ============================================================
     * TOTALS
     * ============================================================
     */

    @Column(
            name = "snapshot_subtotal_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal subtotalAmount;

    @Column(
            name = "snapshot_gst_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal gstAmount;

    @Column(
            name = "snapshot_total_amount",
            precision = 14,
            scale = 2
    )
    private BigDecimal totalAmount;

    public WorkLogFinancialSnapshot() {
    }

    public boolean isCreated() {
        return totalAmount != null;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerAbn() {
        return workerAbn;
    }

    public void setWorkerAbn(String workerAbn) {
        this.workerAbn = workerAbn;
    }

    public Boolean getWorkerGstRegistered() {
        return workerGstRegistered;
    }

    public void setWorkerGstRegistered(
            Boolean workerGstRegistered
    ) {
        this.workerGstRegistered =
                workerGstRegistered;
    }

    public BigDecimal getRegularRate() {
        return regularRate;
    }

    public void setRegularRate(BigDecimal regularRate) {
        this.regularRate = regularRate;
    }

    public BigDecimal getOvertime15Rate() {
        return overtime15Rate;
    }

    public void setOvertime15Rate(
            BigDecimal overtime15Rate
    ) {
        this.overtime15Rate = overtime15Rate;
    }

    public BigDecimal getOvertime20Rate() {
        return overtime20Rate;
    }

    public void setOvertime20Rate(
            BigDecimal overtime20Rate
    ) {
        this.overtime20Rate = overtime20Rate;
    }

    public BigDecimal getSaturdayRate() {
        return saturdayRate;
    }

    public void setSaturdayRate(
            BigDecimal saturdayRate
    ) {
        this.saturdayRate = saturdayRate;
    }

    public BigDecimal getSundayRate() {
        return sundayRate;
    }

    public void setSundayRate(
            BigDecimal sundayRate
    ) {
        this.sundayRate = sundayRate;
    }

    public BigDecimal getPublicHolidayRate() {
        return publicHolidayRate;
    }

    public void setPublicHolidayRate(
            BigDecimal publicHolidayRate
    ) {
        this.publicHolidayRate =
                publicHolidayRate;
    }

    public BigDecimal getTravelRate() {
        return travelRate;
    }

    public void setTravelRate(
            BigDecimal travelRate
    ) {
        this.travelRate = travelRate;
    }

    public BigDecimal getKilometreRate() {
        return kilometreRate;
    }

    public void setKilometreRate(
            BigDecimal kilometreRate
    ) {
        this.kilometreRate = kilometreRate;
    }

    public BigDecimal getLafhaRate() {
        return lafhaRate;
    }

    public void setLafhaRate(
            BigDecimal lafhaRate
    ) {
        this.lafhaRate = lafhaRate;
    }

    public BigDecimal getRegularAmount() {
        return regularAmount;
    }

    public void setRegularAmount(
            BigDecimal regularAmount
    ) {
        this.regularAmount = regularAmount;
    }

    public BigDecimal getOvertime15Amount() {
        return overtime15Amount;
    }

    public void setOvertime15Amount(
            BigDecimal overtime15Amount
    ) {
        this.overtime15Amount =
                overtime15Amount;
    }

    public BigDecimal getOvertime20Amount() {
        return overtime20Amount;
    }

    public void setOvertime20Amount(
            BigDecimal overtime20Amount
    ) {
        this.overtime20Amount =
                overtime20Amount;
    }

    public BigDecimal getSaturdayAmount() {
        return saturdayAmount;
    }

    public void setSaturdayAmount(
            BigDecimal saturdayAmount
    ) {
        this.saturdayAmount = saturdayAmount;
    }

    public BigDecimal getSundayAmount() {
        return sundayAmount;
    }

    public void setSundayAmount(
            BigDecimal sundayAmount
    ) {
        this.sundayAmount = sundayAmount;
    }

    public BigDecimal getPublicHolidayAmount() {
        return publicHolidayAmount;
    }

    public void setPublicHolidayAmount(
            BigDecimal publicHolidayAmount
    ) {
        this.publicHolidayAmount =
                publicHolidayAmount;
    }

    public BigDecimal getTravelAmount() {
        return travelAmount;
    }

    public void setTravelAmount(
            BigDecimal travelAmount
    ) {
        this.travelAmount = travelAmount;
    }

    public BigDecimal getKilometreAmount() {
        return kilometreAmount;
    }

    public void setKilometreAmount(
            BigDecimal kilometreAmount
    ) {
        this.kilometreAmount =
                kilometreAmount;
    }

    public BigDecimal getLafhaAmount() {
        return lafhaAmount;
    }

    public void setLafhaAmount(
            BigDecimal lafhaAmount
    ) {
        this.lafhaAmount = lafhaAmount;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(
            BigDecimal subtotalAmount
    ) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(
            BigDecimal gstAmount
    ) {
        this.gstAmount = gstAmount;
    }

    public Boolean getGstApplied() {
        return gstApplied;
    }

    public void setGstApplied(Boolean gstApplied) {
        this.gstApplied = gstApplied;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount = totalAmount;
    }
}
