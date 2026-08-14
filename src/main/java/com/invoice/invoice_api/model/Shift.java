package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.ShiftMode;
import com.invoice.invoice_api.enums.ShiftStatus;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "shifts", indexes = {
        @Index(name = "idx_shift_company_date", columnList = "company_id,shift_date"),
        @Index(name = "idx_shift_company_status", columnList = "company_id,status")
})
public class Shift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_position_id", nullable = false)
    private ProjectPosition projectPosition;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_user_id", nullable = false)
    private AppUser createdBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ShiftMode mode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ShiftStatus status;
    @Column(name = "shift_date", nullable = false) private LocalDate shiftDate;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(nullable = false) private Integer capacity;
    @Column(length = 255) private String location;
    @Column(length = 1000) private String notes;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @PrePersist void prePersist() { var now = LocalDateTime.now(); createdAt = now; updatedAt = now; if (status == null) status = ShiftStatus.OPEN; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Company getCompany(){return company;} public void setCompany(Company v){company=v;}
    public ProjectPosition getProjectPosition(){return projectPosition;} public void setProjectPosition(ProjectPosition v){projectPosition=v;}
    public AppUser getCreatedBy(){return createdBy;} public void setCreatedBy(AppUser v){createdBy=v;}
    public ShiftMode getMode(){return mode;} public void setMode(ShiftMode v){mode=v;}
    public ShiftStatus getStatus(){return status;} public void setStatus(ShiftStatus v){status=v;}
    public LocalDate getShiftDate(){return shiftDate;} public void setShiftDate(LocalDate v){shiftDate=v;}
    public LocalTime getStartTime(){return startTime;} public void setStartTime(LocalTime v){startTime=v;}
    public LocalTime getEndTime(){return endTime;} public void setEndTime(LocalTime v){endTime=v;}
    public Integer getCapacity(){return capacity;} public void setCapacity(Integer v){capacity=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
