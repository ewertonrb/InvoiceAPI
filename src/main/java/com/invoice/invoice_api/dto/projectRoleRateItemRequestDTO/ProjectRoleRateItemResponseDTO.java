package com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO;

import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public record ProjectRoleRateItemResponseDTO(
        Long id,

        RateType rateType,

        RateCalculationType calculationType,

        BigDecimal value,

        String description,

        Boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
