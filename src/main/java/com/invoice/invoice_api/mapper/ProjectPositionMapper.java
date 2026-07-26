package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.projectPosition.ProjectPositionRequestDTO;
import com.invoice.invoice_api.dto.projectPosition.ProjectPositionResponseDTO;
import com.invoice.invoice_api.model.ProjectPosition;

public final class ProjectPositionMapper {

    private ProjectPositionMapper() {
    }

    public static ProjectPositionResponseDTO toResponseDTO(ProjectPosition position) {

        if (position == null) {
            return null;
        }

        return new ProjectPositionResponseDTO(
                position.getId(),
                position.getPositionName(),

                position.getProject().getId(),
                position.getProject().getName(),

                position.getProject().getCompany().getId(),
                position.getProject().getCompany().getName(),

                position.getActive(),
                position.getCreatedAt(),
                position.getUpdatedAt()
        );
    }
}
