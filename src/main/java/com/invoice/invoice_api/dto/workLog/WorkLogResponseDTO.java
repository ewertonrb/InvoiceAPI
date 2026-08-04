package com.invoice.invoice_api.dto.workLog;

import com.invoice.invoice_api.enums.WorkLogStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record WorkLogResponseDTO(
        Long id,

        Long workerProfileId,

        Long appUserId,

        String workerName,

        String workerEmail,

        Long projectPositionId,

        String positionName,

        Long projectId,

        String projectName,

        Long companyId,

        String companyName,

        LocalDate workDate,

        WorkLogTimeResponseDTO workTime,

        BigDecimal regularHours,

        BigDecimal overtime15Hours,

        BigDecimal overtime20Hours,

        BigDecimal saturdayHours,

        BigDecimal sundayHours,

        BigDecimal publicHolidayHours,

        WorkLogTravelResponseDTO travel,

        WorkLogFinancialSnapshotResponseDTO financialSnapshot,

        String notes,

        String managerNotes,

        WorkLogStatus status,

        LocalDateTime submittedAt,

        LocalDateTime approvedAt,

        LocalDateTime rejectedAt,

        String rejectionReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt


) {
}
