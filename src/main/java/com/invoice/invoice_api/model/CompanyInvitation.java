package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.InvitationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_invitations",
        indexes = {
                @Index(
                        name = "idx_invitation_company_status",
                        columnList = "company_id,status"
                ),
                @Index(
                        name = "idx_invitation_email",
                        columnList = "email"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invitation_token_hash",
                        columnNames = "token_hash"
                )
        }
)
public class CompanyInvitation {

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
                    name = "fk_invitation_company"
            )
    )
    private Company company;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            length = 100
    )
    private String surname;

    @Column(
            nullable = false,
            length = 150
    )
    private String email;

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
    private InvitationStatus status;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "invited_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_invitation_invited_by"
            )
    )
    private AppUser invitedBy;

    @Column(
            name = "token_hash",
            nullable = false,
            length = 64
    )
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime cancelledAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CompanyInvitation() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = InvitationStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null
                && expiresAt.isBefore(LocalDateTime.now());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CompanyRole getRole() {
        return role;
    }

    public void setRole(CompanyRole role) {
        this.role = role;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public AppUser getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(AppUser invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getDeclinedAt() {return declinedAt;}

    public void setDeclinedAt(LocalDateTime declinedAt) {this.declinedAt = declinedAt;}

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
