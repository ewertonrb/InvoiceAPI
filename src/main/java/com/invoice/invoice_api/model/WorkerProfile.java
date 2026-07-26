package com.invoice.invoice_api.model;

import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.SuperDetails;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "worker_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_worker_profile_app_user",
                        columnNames = "app_user_id"
                ),
                @UniqueConstraint(
                        name = "uk_worker_profile_abn",
                        columnNames = "abn"
                )
        }
)
public class WorkerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "app_user_id",
            nullable = false,
            unique = true
    )
    private AppUser appUser;

    @Column(length = 11, unique = true)
    private String abn;

    @Column(name = "gst_registered", nullable = false)
    private Boolean gstRegistered = false;

    @Column(length = 30)
    private String phone;

    @Column(name = "default_hourly_rate", precision = 12, scale = 2)
    private BigDecimal defaultHourlyRate;

    @Embedded
    private BankDetails bankDetails;

    @Embedded
    private SuperDetails superDetails;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 1000)
    private String notes;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WorkerProfile() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }

        if (gstRegistered == null) {
            gstRegistered = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public String getAbn() {
        return abn;
    }

    public void setAbn(String abn) {
        this.abn = abn;
    }

    public Boolean getGstRegistered() {
        return gstRegistered;
    }

    public void setGstRegistered(Boolean gstRegistered) {
        this.gstRegistered = gstRegistered;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(BigDecimal defaultHourlyRate) {
        this.defaultHourlyRate = defaultHourlyRate;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public SuperDetails getSuperDetails() {
        return superDetails;
    }

    public void setSuperDetails(SuperDetails superDetails) {
        this.superDetails = superDetails;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
