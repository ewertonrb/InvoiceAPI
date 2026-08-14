package com.invoice.invoice_api.dto.notification;

import com.invoice.invoice_api.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponseDTO(Long id, NotificationType type, String title, String message, String targetPath, Long relatedShiftId, boolean read, LocalDateTime createdAt) {}
