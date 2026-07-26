package com.invoice.invoice_api.dto.projectRoleRate;

import com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO.ProjectRoleRateItemResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectRoleRateResponseDTO(
        Long id,

        Long projectPositionId,
        String positionName,

        Long projectId,
        String projectName,

        Long companyId,
        String companyName,

        LocalDate effectiveFrom,
        LocalDate effectiveTo,

        Boolean active,

        List<ProjectRoleRateItemResponseDTO> items,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
