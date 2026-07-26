package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateResponseDTO;
import com.invoice.invoice_api.model.*;

public class ProjectRoleRateMapper {
    private ProjectRoleRateMapper() {
    }

    public static ProjectRoleRateResponseDTO toResponseDTO(
            ProjectRoleRate rate
    ) {
        ProjectPosition position =
                rate.getProjectPosition();

        Project project =
                position.getProject();

        Company company =
                project.getCompany();

        return new ProjectRoleRateResponseDTO(
                rate.getId(),

                position.getId(),
                position.getPositionName(),

                project.getId(),
                project.getName(),

                company.getId(),
                company.getName(),

                rate.getEffectiveFrom(),
                rate.getEffectiveTo(),

                rate.getActive(),

                rate.getItems()
                        .stream()
                        .map(
                                ProjectRoleRateItemMapper
                                        ::toResponseDTO
                        )
                        .toList(),

                rate.getCreatedAt(),
                rate.getUpdatedAt()
        );
    }
    private static int getDisplayOrder(
            ProjectRoleRateItem item
    ) {
        return switch (item.getRateType()) {
            case REGULAR -> 1;
            case OVERTIME_1_5 -> 2;
            case OVERTIME_2_0 -> 3;
            case SATURDAY -> 4;
            case SUNDAY -> 5;
            case PUBLIC_HOLIDAY -> 6;
            case TRAVEL_TIME -> 7;
            case KILOMETRE -> 8;
            case LAFHA -> 9;
        };
    }
}
