package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_membership_user_company",
                        columnNames = {"app_user_id", "company_id"}
                )
        }
)
public class CompanyMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "app_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_app_user")
    )
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_membership_company")
    )
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompanyRole role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CompanyMembership() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanyRole getRole() {
        return role;
    }

    public void setRole(CompanyRole role) {
        this.role = role;
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
}
