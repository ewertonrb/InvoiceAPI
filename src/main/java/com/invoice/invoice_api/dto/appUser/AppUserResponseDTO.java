package com.invoice.invoice_api.dto.appUser;

import java.time.LocalDateTime;

public record AppUserResponseDTO(
        Long id,
        String name,
        String surname,
        String email,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
