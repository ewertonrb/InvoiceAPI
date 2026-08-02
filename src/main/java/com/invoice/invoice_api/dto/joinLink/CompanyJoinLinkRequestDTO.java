package com.invoice.invoice_api.dto.joinLink;

import com.invoice.invoice_api.enums.CompanyRole;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CompanyJoinLinkRequestDTO(

        @NotNull(message = "Role is required.")
        CompanyRole role,

        @Min(
                value = 0,
                message = "Maximum uses cannot be negative."
        )
        Integer maxUses,

        LocalDateTime expiresAt

) {
}
