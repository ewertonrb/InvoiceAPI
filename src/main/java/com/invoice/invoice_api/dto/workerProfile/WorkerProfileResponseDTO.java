package com.invoice.invoice_api.dto.workerProfile;

import com.invoice.invoice_api.enums.WorkerProfileStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkerProfileResponseDTO(
        Long id,

        Long appUserId,

        String fullName,

        String email,

        String abn,

        Boolean gstRegistered,

        String phone,

        WorkerProfileStatus status,

        LocalDateTime completedAt,

        BankDetailsResponseDTO bankDetails,

        SuperDetailsResponseDTO superDetails,

        String notes,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
