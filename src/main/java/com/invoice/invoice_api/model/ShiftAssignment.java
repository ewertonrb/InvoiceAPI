package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.ShiftAssignmentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_shift_assignment_worker", columnNames = {"shift_id", "worker_profile_id"}), indexes = @Index(name = "idx_shift_assignment_worker_status", columnList = "worker_profile_id,status"))
public class ShiftAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "shift_id", nullable = false) private Shift shift;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "worker_profile_id", nullable = false) private WorkerProfile workerProfile;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ShiftAssignmentStatus status;
    @Column(length = 500) private String declineReason;
    @Column private LocalDateTime respondedAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @PrePersist void prePersist(){var now=LocalDateTime.now();createdAt=now;updatedAt=now;if(status==null)status=ShiftAssignmentStatus.PENDING;}
    @PreUpdate void preUpdate(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Shift getShift(){return shift;} public void setShift(Shift v){shift=v;}
    public WorkerProfile getWorkerProfile(){return workerProfile;} public void setWorkerProfile(WorkerProfile v){workerProfile=v;}
    public ShiftAssignmentStatus getStatus(){return status;} public void setStatus(ShiftAssignmentStatus v){status=v;}
    public String getDeclineReason(){return declineReason;} public void setDeclineReason(String v){declineReason=v;}
    public LocalDateTime getRespondedAt(){return respondedAt;} public void setRespondedAt(LocalDateTime v){respondedAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
