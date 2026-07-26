package com.invoice.invoice_api.dto.auth;

public record CurrentUserResponseDTO(
        Long id,
        String name,
        String email,
        Boolean active
) {
}
