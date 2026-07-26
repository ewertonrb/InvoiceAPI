package com.invoice.invoice_api.dto.project;

import java.time.LocalDateTime;

public record ProjectResponseDTO(
        Long id,
        String name,

        Long companyId,
        String companyName,

        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
