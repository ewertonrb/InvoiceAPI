package com.invoice.invoice_api.dto.shift;
import com.invoice.invoice_api.enums.ShiftAssignmentStatus;
import java.time.LocalDateTime;
public record ShiftAssignmentResponseDTO(Long id, Long workerProfileId, String workerName, ShiftAssignmentStatus status, String declineReason, LocalDateTime respondedAt) {}
