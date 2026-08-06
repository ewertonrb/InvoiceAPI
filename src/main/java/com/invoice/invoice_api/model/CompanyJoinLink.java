package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.JoinLinkStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_join_links",
        indexes = {
                @Index(
                        name = "idx_join_link_company_status",
                        columnList = "company_id,status"
                ),
                @Index(
                        name = "idx_join_link_token_hash",
                        columnList = "token_hash"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_join_link_token_hash",
                        columnNames = "token_hash"
                )
        }
)
public class CompanyJoinLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_join_link_company"
            )
    )
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private CompanyRole role;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private JoinLinkStatus status;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "created_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_join_link_created_by"
            )
    )
    private AppUser createdBy;

    @Column(
            name = "token_hash",
            nullable = false,
            length = 64
    )
    private String tokenHash;

    @Column(name = "encrypted_token", length = 512)
    private String encryptedToken;

    @Column(
            name = "max_uses",
            nullable = false
    )
    private Integer maxUses;

    @Column(
            name = "current_uses",
            nullable = false
    )
    private Integer currentUses = 0;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            name = "disabled_at"
    )
    private LocalDateTime disabledAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CompanyJoinLink() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = JoinLinkStatus.ACTIVE;
        }

        if (currentUses == null) {
            currentUses = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean hasReachedUsageLimit() {
        return currentUses >= maxUses;
    }

    public boolean isAvailable() {
        return status == JoinLinkStatus.ACTIVE
                && !isExpired()
                && !hasReachedUsageLimit();
    }

    public void registerUse() {
        if (currentUses == null) {
            currentUses = 0;
        }

        currentUses++;
    }

    public Long getId() {
        return id;
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

    public JoinLinkStatus getStatus() {
        return status;
    }

    public void setStatus(JoinLinkStatus status) {
        this.status = status;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getEncryptedToken() { return encryptedToken; }
    public void setEncryptedToken(String encryptedToken) { this.encryptedToken = encryptedToken; }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(Integer currentUses) {
        this.currentUses = currentUses;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getDisabledAt() {
        return disabledAt;
    }

    public void setDisabledAt(LocalDateTime disabledAt) {
        this.disabledAt = disabledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
