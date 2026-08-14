package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_company_created", columnList = "app_user_id,company_id,created_at"),
        @Index(name = "idx_notification_user_unread", columnList = "app_user_id,read_at")
})
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "app_user_id", nullable = false) private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false) private Company company;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private NotificationType type;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 1000) private String message;
    @Column(name = "target_path", nullable = false, length = 255) private String targetPath;
    @Column(name = "related_shift_id") private Long relatedShiftId;
    @Column(name = "read_at") private LocalDateTime readAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public AppUser getUser() { return user; } public void setUser(AppUser user) { this.user = user; }
    public Company getCompany() { return company; } public void setCompany(Company company) { this.company = company; }
    public NotificationType getType() { return type; } public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public String getTargetPath() { return targetPath; } public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public Long getRelatedShiftId() { return relatedShiftId; } public void setRelatedShiftId(Long relatedShiftId) { this.relatedShiftId = relatedShiftId; }
    public LocalDateTime getReadAt() { return readAt; } public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
