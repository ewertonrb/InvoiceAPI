package com.invoice.invoice_api.dto.workerProfile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkerProfileResponseDTO(
        Long id,

        Long appUserId,
        String appUserName,
        String appUserEmail,

        String abn,
        Boolean gstRegistered,
        String phone,
        BigDecimal defaultHourlyRate,

        BankDetailsResponseDTO bankDetails,
        SuperDetailsResponseDTO superDetails,

        Boolean active,
        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
