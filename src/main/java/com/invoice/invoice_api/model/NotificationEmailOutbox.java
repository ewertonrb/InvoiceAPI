package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.NotificationDeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_email_outbox", indexes = @Index(name = "idx_notification_outbox_pending", columnList = "status,next_attempt_at"))
public class NotificationEmailOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "notification_id", nullable = false) private Notification notification;
    @Column(nullable = false, length = 150) private String recipient;
    @Column(nullable = false, length = 200) private String subject;
    @Column(nullable = false, length = 5000) private String body;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NotificationDeliveryStatus status;
    @Column(nullable = false) private Integer attempts;
    @Column(name = "next_attempt_at", nullable = false) private LocalDateTime nextAttemptAt;
    @Column(name = "last_error", length = 1000) private String lastError;
    @Column(name = "sent_at") private LocalDateTime sentAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (attempts == null) attempts = 0; if (status == null) status = NotificationDeliveryStatus.PENDING; if (nextAttemptAt == null) nextAttemptAt = createdAt; }
    public Long getId() { return id; }
    public Notification getNotification() { return notification; } public void setNotification(Notification notification) { this.notification = notification; }
    public String getRecipient() { return recipient; } public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; } public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; } public void setBody(String body) { this.body = body; }
    public NotificationDeliveryStatus getStatus() { return status; } public void setStatus(NotificationDeliveryStatus status) { this.status = status; }
    public Integer getAttempts() { return attempts; } public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public LocalDateTime getNextAttemptAt() { return nextAttemptAt; } public void setNextAttemptAt(LocalDateTime nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public String getLastError() { return lastError; } public void setLastError(String lastError) { this.lastError = lastError; }
    public LocalDateTime getSentAt() { return sentAt; } public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
