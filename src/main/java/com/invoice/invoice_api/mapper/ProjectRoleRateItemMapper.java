package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO.ProjectRoleRateItemResponseDTO;
import com.invoice.invoice_api.model.ProjectRoleRateItem;

public class ProjectRoleRateItemMapper {
    private ProjectRoleRateItemMapper() {
    }

    public static ProjectRoleRateItemResponseDTO toResponseDTO(
            ProjectRoleRateItem item
    ) {
        return new ProjectRoleRateItemResponseDTO(
                item.getId(),
                item.getRateType(),
                item.getCalculationType(),
                item.getValue(),
                item.getDescription(),
                item.getActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
