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

        BigDecimal regularHours,

        BigDecimal overtime15Hours,

        BigDecimal overtime20Hours,

        BigDecimal saturdayHours,

        BigDecimal sundayHours,

        BigDecimal publicHolidayHours,

        BigDecimal travelHours,

        BigDecimal kilometres,

        Integer lafhaNights,

        WorkLogStatus status,

        LocalDateTime submittedAt,

        LocalDateTime approvedAt,

        LocalDateTime rejectedAt,

        String rejectionReason

) {
}
