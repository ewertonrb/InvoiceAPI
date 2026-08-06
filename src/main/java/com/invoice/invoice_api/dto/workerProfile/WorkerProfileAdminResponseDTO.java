package com.invoice.invoice_api.dto.workerProfile;

import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.WorkerProfileStatus;

import java.time.LocalDateTime;

public record WorkerProfileAdminResponseDTO(
        Long id,
        Long appUserId,
        String fullName,
        String email,
        String abn,
        Boolean gstRegistered,
        String phone,
        WorkerProfileStatus status,
        Long membershipId,
        CompanyRole membershipRole,
        MembershipStatus membershipStatus,
        LocalDateTime completedAt,
        BankDetailsResponseDTO bankDetails,
        SuperDetailsResponseDTO superDetails,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
