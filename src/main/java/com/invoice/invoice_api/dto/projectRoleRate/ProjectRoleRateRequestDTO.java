package com.invoice.invoice_api.dto.projectRoleRate;

import com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO.ProjectRoleRateItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ProjectRoleRateRequestDTO(
        @NotNull(message = "Project position ID is required")
        Long projectPositionId,

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @NotEmpty(message = "At least one rate item is required")
        List<@Valid ProjectRoleRateItemRequestDTO> items

) {
}
