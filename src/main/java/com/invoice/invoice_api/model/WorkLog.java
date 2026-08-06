package com.invoice.invoice_api.model;


import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTime;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTravel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "work_logs",
        indexes = {
                @Index(
                        name = "idx_work_log_worker_date",
                        columnList = "worker_profile_id, work_date"
                ),
                @Index(
                        name = "idx_work_log_position_date",
                        columnList = "project_position_id, work_date"
                ),
                @Index(
                        name = "idx_work_log_status",
                        columnList = "status"
                )
        }
)
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ============================================================
     * RELATIONSHIPS
     * ============================================================
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_work_log_worker_profile"
            )
    )
    private WorkerProfile workerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_position_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_work_log_project_position"
            )
    )
    private ProjectPosition projectPosition;

    /*
     * ============================================================
     * OPERATIONAL DATA
     * ============================================================
     */

    @Column(
            name = "work_date",
            nullable = false
    )
    private LocalDate workDate;

    @Embedded
    private WorkLogTime workTime =
            new WorkLogTime();

    /*
     * ============================================================
     * APPROVED HOUR CATEGORIES
     * ============================================================
     */

    @Column(
            name = "regular_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal regularHours =
            BigDecimal.ZERO;

    @Column(
            name = "overtime_15_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal overtime15Hours =
            BigDecimal.ZERO;

    @Column(
            name = "overtime_20_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal overtime20Hours =
            BigDecimal.ZERO;

    @Column(
            name = "saturday_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal saturdayHours =
            BigDecimal.ZERO;

    @Column(
            name = "sunday_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal sundayHours =
            BigDecimal.ZERO;

    @Column(
            name = "public_holiday_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal publicHolidayHours =
            BigDecimal.ZERO;

    /*
     * ============================================================
     * TRAVEL / ALLOWANCES
     * ============================================================
     */

    @Embedded
    private WorkLogTravel travel =
            new WorkLogTravel();

    /*
     * ============================================================
     * FINANCIAL SNAPSHOT
     * ============================================================
     */

    @Embedded
    private WorkLogFinancialSnapshot financialSnapshot;

    /*
     * ============================================================
     * NOTES
     * ============================================================
     */

    @Column(
            name = "notes",
            length = 500
    )
    private String notes;

    @Column(
            name = "manager_notes",
            length = 1000
    )
    private String managerNotes;

    /*
     * ============================================================
     * WORKFLOW
     * ============================================================
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private WorkLogStatus status =
            WorkLogStatus.PENDING_APPROVAL;

    @Column(
            name = "submitted_at",
            nullable = false
    )
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(
            name = "rejection_reason",
            length = 500
    )
    private String rejectionReason;

    /*
     * ============================================================
     * AUDIT
     * ============================================================
     */

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public WorkLog() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        if (status == null) {
            status =
                    WorkLogStatus.PENDING_APPROVAL;
        }

        if (submittedAt == null) {
            submittedAt = now;
        }

        createdAt = now;
        updatedAt = now;

        initializeNullValues();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt =
                LocalDateTime.now();

        initializeNullValues();
    }

    private void initializeNullValues() {
        regularHours =
                zeroIfNull(regularHours);

        overtime15Hours =
                zeroIfNull(overtime15Hours);

        overtime20Hours =
                zeroIfNull(overtime20Hours);

        saturdayHours =
                zeroIfNull(saturdayHours);

        sundayHours =
                zeroIfNull(sundayHours);

        publicHolidayHours =
                zeroIfNull(publicHolidayHours);

        if (workTime == null) {
            workTime =
                    new WorkLogTime();
        }

        if (travel == null) {
            travel =
                    new WorkLogTravel();
        }

    }

    private BigDecimal zeroIfNull(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    /*
     * ============================================================
     * DOMAIN HELPERS
     * ============================================================
     */

    public boolean hasFinancialSnapshot() {
        return financialSnapshot != null
                && financialSnapshot.isCreated();
    }

    public boolean isPendingApproval() {
        return status
                == WorkLogStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return status
                == WorkLogStatus.APPROVED;
    }

    public boolean isRejected() {
        return status
                == WorkLogStatus.REJECTED;
    }

    /*
     * ============================================================
     * GETTERS / SETTERS
     * ============================================================
     */

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public WorkerProfile getWorkerProfile() {
        return workerProfile;
    }

    public void setWorkerProfile(
            WorkerProfile workerProfile
    ) {
        this.workerProfile =
                workerProfile;
    }

    public ProjectPosition getProjectPosition() {
        return projectPosition;
    }

    public void setProjectPosition(
            ProjectPosition projectPosition
    ) {
        this.projectPosition =
                projectPosition;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(
            LocalDate workDate
    ) {
        this.workDate = workDate;
    }

    public WorkLogTime getWorkTime() {
        return workTime;
    }

    public void setWorkTime(
            WorkLogTime workTime
    ) {
        this.workTime =
                workTime == null
                        ? new WorkLogTime()
                        : workTime;
    }

    public BigDecimal getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(
            BigDecimal regularHours
    ) {
        this.regularHours =
                zeroIfNull(regularHours);
    }

    public BigDecimal getOvertime15Hours() {
        return overtime15Hours;
    }

    public void setOvertime15Hours(
            BigDecimal overtime15Hours
    ) {
        this.overtime15Hours =
                zeroIfNull(overtime15Hours);
    }

    public BigDecimal getOvertime20Hours() {
        return overtime20Hours;
    }

    public void setOvertime20Hours(
            BigDecimal overtime20Hours
    ) {
        this.overtime20Hours =
                zeroIfNull(overtime20Hours);
    }

    public BigDecimal getSaturdayHours() {
        return saturdayHours;
    }

    public void setSaturdayHours(
            BigDecimal saturdayHours
    ) {
        this.saturdayHours =
                zeroIfNull(saturdayHours);
    }

    public BigDecimal getSundayHours() {
        return sundayHours;
    }

    public void setSundayHours(
            BigDecimal sundayHours
    ) {
        this.sundayHours =
                zeroIfNull(sundayHours);
    }

    public BigDecimal getPublicHolidayHours() {
        return publicHolidayHours;
    }

    public void setPublicHolidayHours(
            BigDecimal publicHolidayHours
    ) {
        this.publicHolidayHours =
                zeroIfNull(publicHolidayHours);
    }

    public WorkLogTravel getTravel() {
        return travel;
    }

    public void setTravel(
            WorkLogTravel travel
    ) {
        this.travel =
                travel == null
                        ? new WorkLogTravel()
                        : travel;
    }

    /*
     * Métodos de compatibilidade temporários.
     *
     * Eles evitam quebrar DTOs, mappers e services antigos enquanto
     * migramos o restante do módulo para workLog.getTravel().
     */

    public BigDecimal getTravelHours() {
        return getTravel()
                .getTravelHours();
    }

    public void setTravelHours(
            BigDecimal travelHours
    ) {
        getTravel()
                .setTravelHours(travelHours);
    }

    public BigDecimal getKilometres() {
        return getTravel()
                .getKilometres();
    }

    public void setKilometres(
            BigDecimal kilometres
    ) {
        getTravel()
                .setKilometres(kilometres);
    }

    public Integer getLafhaNights() {
        return getTravel()
                .getLafhaNights();
    }

    public void setLafhaNights(
            Integer lafhaNights
    ) {
        getTravel()
                .setLafhaNights(lafhaNights);
    }

    public WorkLogFinancialSnapshot getFinancialSnapshot() {
        return financialSnapshot;
    }

    public void setFinancialSnapshot(
            WorkLogFinancialSnapshot financialSnapshot
    ) {
        if (hasFinancialSnapshot()
                && financialSnapshot != this.financialSnapshot) {
            throw new IllegalStateException(
                    "Financial snapshots are immutable once created"
            );
        }

        this.financialSnapshot =
                financialSnapshot;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes
    ) {
        this.notes = notes;
    }

    public String getManagerNotes() {
        return managerNotes;
    }

    public void setManagerNotes(
            String managerNotes
    ) {
        this.managerNotes =
                managerNotes;
    }

    public WorkLogStatus getStatus() {
        return status;
    }

    public void setStatus(
            WorkLogStatus status
    ) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(
            LocalDateTime submittedAt
    ) {
        this.submittedAt =
                submittedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(
            LocalDateTime approvedAt
    ) {
        this.approvedAt =
                approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(
            LocalDateTime rejectedAt
    ) {
        this.rejectedAt =
                rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(
            String rejectionReason
    ) {
        this.rejectionReason =
                rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }
    public boolean isInvoiced() {
        return status == WorkLogStatus.INVOICED;
    }

    public boolean isCancelled() {
        return status == WorkLogStatus.CANCELLED;
    }

    public boolean canBeEdited() {
        return status == WorkLogStatus.PENDING_APPROVAL
                || status == WorkLogStatus.REJECTED;
    }

    public boolean isFinalized() {
        return status == WorkLogStatus.INVOICED
                || status == WorkLogStatus.CANCELLED;
    }
}
