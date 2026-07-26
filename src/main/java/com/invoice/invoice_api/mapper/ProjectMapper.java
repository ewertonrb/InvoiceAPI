package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.project.ProjectResponseDTO;
import com.invoice.invoice_api.model.Project;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponseDTO toResponseDTO(
            Project project
    ) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),

                project.getCompany().getId(),
                project.getCompany().getName(),

                project.getActive(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
