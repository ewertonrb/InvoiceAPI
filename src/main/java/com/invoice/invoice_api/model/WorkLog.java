package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.WorkLogStatus;
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
                )
        }
)
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_profile_id",
            nullable = false
    )
    private WorkerProfile workerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_position_id",
            nullable = false
    )
    private ProjectPosition projectPosition;

    @Column(
            name = "work_date",
            nullable = false
    )
    private LocalDate workDate;

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

    @Column(
            name = "travel_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal travelHours =
            BigDecimal.ZERO;

    @Column(
            name = "kilometres",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal kilometres =
            BigDecimal.ZERO;

    @Column(
            name = "lafha_nights",
            nullable = false
    )
    private Integer lafhaNights = 0;

    @Column(
            name = "notes",
            length = 500
    )
    private String notes;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkLogStatus status = WorkLogStatus.PENDING_APPROVAL;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;


    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = WorkLogStatus.PENDING_APPROVAL;
        }

        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        initializeNullQuantities();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();

        initializeNullQuantities();
    }

    private void initializeNullQuantities() {
        if (regularHours == null) {
            regularHours = BigDecimal.ZERO;
        }

        if (overtime15Hours == null) {
            overtime15Hours = BigDecimal.ZERO;
        }

        if (overtime20Hours == null) {
            overtime20Hours = BigDecimal.ZERO;
        }

        if (saturdayHours == null) {
            saturdayHours = BigDecimal.ZERO;
        }

        if (sundayHours == null) {
            sundayHours = BigDecimal.ZERO;
        }

        if (publicHolidayHours == null) {
            publicHolidayHours = BigDecimal.ZERO;
        }

        if (travelHours == null) {
            travelHours = BigDecimal.ZERO;
        }

        if (kilometres == null) {
            kilometres = BigDecimal.ZERO;
        }

        if (lafhaNights == null) {
            lafhaNights = 0;
        }

        if (active == null) {
            active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkerProfile getWorkerProfile() {
        return workerProfile;
    }

    public void setWorkerProfile(
            WorkerProfile workerProfile
    ) {
        this.workerProfile = workerProfile;
    }

    public ProjectPosition getProjectPosition() {
        return projectPosition;
    }

    public void setProjectPosition(ProjectPosition projectPosition) {
        this.projectPosition = projectPosition;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(BigDecimal regularHours) {
        this.regularHours = regularHours;
    }

    public BigDecimal getOvertime15Hours() {
        return overtime15Hours;
    }

    public void setOvertime15Hours(BigDecimal overtime15Hours) {
        this.overtime15Hours = overtime15Hours;
    }

    public BigDecimal getOvertime20Hours() {
        return overtime20Hours;
    }

    public void setOvertime20Hours(BigDecimal overtime20Hours) {
        this.overtime20Hours = overtime20Hours;
    }

    public BigDecimal getSaturdayHours() {
        return saturdayHours;
    }

    public void setSaturdayHours(BigDecimal saturdayHours) {
        this.saturdayHours = saturdayHours;
    }

    public BigDecimal getSundayHours() {
        return sundayHours;
    }

    public void setSundayHours(BigDecimal sundayHours) {
        this.sundayHours = sundayHours;
    }

    public BigDecimal getPublicHolidayHours() {
        return publicHolidayHours;
    }

    public void setPublicHolidayHours(BigDecimal publicHolidayHours) {
        this.publicHolidayHours = publicHolidayHours;
    }

    public BigDecimal getTravelHours() {
        return travelHours;
    }

    public void setTravelHours(BigDecimal travelHours) {
        this.travelHours = travelHours;
    }

    public BigDecimal getKilometres() {
        return kilometres;
    }

    public void setKilometres(BigDecimal kilometres) {
        this.kilometres = kilometres;
    }

    public Integer getLafhaNights() {
        return lafhaNights;
    }

    public void setLafhaNights(Integer lafhaNights) {
        this.lafhaNights = lafhaNights;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public WorkLogStatus getStatus() {
        return status;
    }

    public void setStatus(WorkLogStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
