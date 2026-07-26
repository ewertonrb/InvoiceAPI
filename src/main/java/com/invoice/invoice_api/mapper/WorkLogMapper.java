package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.model.*;

public final class WorkLogMapper {

    private WorkLogMapper() {
    }

    public static WorkLogResponseDTO toResponseDTO(
            WorkLog workLog
    ) {
        if (workLog == null) {
            return null;
        }

        WorkerProfile workerProfile =
                workLog.getWorkerProfile();

        AppUser appUser =
                workerProfile.getAppUser();

        ProjectPosition projectPosition =
                workLog.getProjectPosition();

        Project project =
                projectPosition.getProject();

        Company company =
                project.getCompany();

        return new WorkLogResponseDTO(
                workLog.getId(),

                workLog.getWorkerProfile().getId(),
                workLog.getWorkerProfile().getAppUser().getId(),
                workLog.getWorkerProfile().getAppUser().getFullName(),
                workLog.getWorkerProfile().getAppUser().getEmail(),

                workLog.getProjectPosition().getId(),
                workLog.getProjectPosition().getPositionName(),

                workLog.getProjectPosition().getProject().getId(),
                workLog.getProjectPosition().getProject().getName(),

                workLog.getProjectPosition().getProject().getCompany().getId(),
                workLog.getProjectPosition().getProject().getCompany().getName(),

                workLog.getWorkDate(),

                workLog.getRegularHours(),
                workLog.getOvertime15Hours(),
                workLog.getOvertime20Hours(),
                workLog.getSaturdayHours(),
                workLog.getSundayHours(),
                workLog.getPublicHolidayHours(),
                workLog.getTravelHours(),
                workLog.getKilometres(),
                workLog.getLafhaNights(),

                workLog.getStatus(),
                workLog.getSubmittedAt(),
                workLog.getApprovedAt(),
                workLog.getRejectedAt(),
                workLog.getRejectionReason()
        );
    }
}
