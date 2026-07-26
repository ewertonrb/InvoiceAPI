package com.invoice.invoice_api.dto.error;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
