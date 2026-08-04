package com.invoice.invoice_api.dto.workerProfile;

import com.invoice.invoice_api.enums.WorkerProfileStatus;

public record WorkerProfileSummaryDTO(
        Long workerProfileId,

        Long appUserId,

        String fullName,

        String email,

        String phone,

        String abn,

        Boolean gstRegistered,

        WorkerProfileStatus status,

        Boolean profileComplete
) {
}
