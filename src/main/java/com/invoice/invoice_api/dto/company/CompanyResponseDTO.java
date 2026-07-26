package com.invoice.invoice_api.dto.company;

import java.time.LocalDateTime;

public record CompanyResponseDTO(
        Long id,
        String name,
        String abn,
        String email,
        String phone,
        String address,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
