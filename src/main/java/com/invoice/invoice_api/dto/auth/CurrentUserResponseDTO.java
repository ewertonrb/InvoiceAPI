package com.invoice.invoice_api.dto.auth;


import com.invoice.invoice_api.enums.UserStatus;

public record CurrentUserResponseDTO(
        Long id,
        String name,
        String surname,
        String email,
        UserStatus status
) {
}
