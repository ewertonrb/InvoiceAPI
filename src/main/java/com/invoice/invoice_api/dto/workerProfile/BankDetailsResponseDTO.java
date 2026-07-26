package com.invoice.invoice_api.dto.workerProfile;

public record BankDetailsResponseDTO(
        String bankName,
        String accountName,
        String bsb,
        String accountNumber
) {
}
