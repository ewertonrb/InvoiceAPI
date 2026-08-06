package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.UserStatus;
import com.invoice.invoice_api.enums.SystemRole;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.sql.Types;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_user_email",
                        columnNames = "email"
                )
        }
)
public class AppUser {
    @JdbcTypeCode(Types.VARBINARY)
    @Column(name = "avatar_data", columnDefinition = "bytea")
    private byte[] avatarData;

    @Column(name = "avatar_content_type", length = 80)
    private String avatarContentType;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column
    private String password;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", nullable = false, length = 30)
    private SystemRole systemRole = SystemRole.USER;

    public AppUser() {
    }

    public String getFullName() {
        String firstName =
                name == null
                        ? ""
                        : name.trim();

        String lastName =
                surname == null
                        ? ""
                        : surname.trim();

        return (firstName + " " + lastName).trim();
    }
    public byte[] getAvatarData() { return avatarData; }
    public void setAvatarData(byte[] avatarData) { this.avatarData = avatarData; }
    public String getAvatarContentType() { return avatarContentType; }
    public void setAvatarContentType(String avatarContentType) { this.avatarContentType = avatarContentType; }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = UserStatus.ACTIVE;
        }

        if (systemRole == null) {
            systemRole = SystemRole.USER;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(SystemRole systemRole) {
        this.systemRole = systemRole;
    }
}
