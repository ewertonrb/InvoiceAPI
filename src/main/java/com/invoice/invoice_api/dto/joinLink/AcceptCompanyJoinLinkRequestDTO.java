package com.invoice.invoice_api.dto.joinLink;

import jakarta.validation.constraints.NotBlank;

public record AcceptCompanyJoinLinkRequestDTO(
        @NotBlank(message = "Join link token is required.")
        String token
) {
    /**
     * Source-compatible bridge for callers compiled against the old unsafe
     * payload. Identity arguments are deliberately ignored.
     */
    public AcceptCompanyJoinLinkRequestDTO(
            String token,
            String ignoredName,
            String ignoredSurname,
            String ignoredEmail,
            String ignoredPassword,
            String ignoredConfirmPassword
    ) {
        this(token);
    }
}
