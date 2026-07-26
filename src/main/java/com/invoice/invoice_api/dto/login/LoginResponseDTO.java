package com.invoice.invoice_api.dto.login;

public record LoginResponseDTO(
        String token,
        String tokenType,
        Long expiresIn,
        Long userId,
        String name,
        String email
) {
}
