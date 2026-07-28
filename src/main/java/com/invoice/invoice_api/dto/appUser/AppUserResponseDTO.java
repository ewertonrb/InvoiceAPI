package com.invoice.invoice_api.dto.appUser;

import com.invoice.invoice_api.enums.UserStatus;

import java.time.LocalDateTime;

public record AppUserResponseDTO(
        Long id,
        String name,
        String surname,
        String email,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
