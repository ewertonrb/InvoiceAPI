package com.invoice.invoice_api.dto.projectPosition;

import java.time.LocalDateTime;

public record ProjectPositionResponseDTO(

        Long id,
        String positionName,

        Long projectId,
        String projectName,

        Long companyId,
        String companyName,

        Boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
