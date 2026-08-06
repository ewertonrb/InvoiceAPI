package com.invoice.invoice_api.dto.joinLink;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CompanyJoinLinkRequestDTO(

        @NotNull(message = "Role is required.")
        CompanyRole role,

        @NotNull(message = "Maximum uses is required.")
        @Min(
                value = 1,
                message = "Maximum uses must be greater than zero."
        )
        Integer maxUses,

        @NotNull(message = "Expiration date is required.")
        @Future(message = "Expiration date must be in the future.")
        LocalDateTime expiresAt

) {
}
