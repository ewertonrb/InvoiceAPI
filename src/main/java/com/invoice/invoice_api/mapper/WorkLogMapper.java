package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workLog.WorkLogFinancialSnapshotResponseDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogTimeResponseDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogTravelResponseDTO;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTime;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTravel;

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

                workerProfile.getId(),

                appUser.getId(),

                appUser.getFullName(),

                appUser.getEmail(),

                projectPosition.getId(),

                projectPosition.getPositionName(),

                project.getId(),

                project.getName(),

                company.getId(),

                company.getName(),

                workLog.getWorkDate(),

                toWorkLogTimeResponseDTO(
                        workLog.getWorkTime()
                ),

                workLog.getRegularHours(),

                workLog.getOvertime15Hours(),

                workLog.getOvertime20Hours(),

                workLog.getSaturdayHours(),

                workLog.getSundayHours(),

                workLog.getPublicHolidayHours(),

                toWorkLogTravelResponseDTO(
                        workLog.getTravel()
                ),

                toFinancialSnapshotResponseDTO(
                        workLog.getFinancialSnapshot()
                ),

                workLog.getNotes(),

                workLog.getManagerNotes(),

                workLog.getStatus(),

                workLog.getSubmittedAt(),

                workLog.getApprovedAt(),

                workLog.getRejectedAt(),

                workLog.getRejectionReason(),

                workLog.getCreatedAt(),

                workLog.getUpdatedAt()
        );
    }

    private static WorkLogTimeResponseDTO toWorkLogTimeResponseDTO(
            WorkLogTime workTime
    ) {
        if (workTime == null) {
            return null;
        }

        return new WorkLogTimeResponseDTO(
                workTime.getStartTime(),

                workTime.getFinishTime(),

                workTime.getUnpaidBreakMinutes(),

                workTime.calculateWorkedMinutes(),

                workTime.crossesMidnight()
        );
    }

    private static WorkLogTravelResponseDTO toWorkLogTravelResponseDTO(
            WorkLogTravel travel
    ) {
        if (travel == null) {
            return null;
        }

        return new WorkLogTravelResponseDTO(
                travel.getTravelHours(),

                travel.getKilometres(),

                travel.getLafhaNights()
        );
    }

    private static WorkLogFinancialSnapshotResponseDTO toFinancialSnapshotResponseDTO(
            WorkLogFinancialSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }

        return new WorkLogFinancialSnapshotResponseDTO(
                snapshot.getCompanyName(),

                snapshot.getProjectName(),

                snapshot.getPositionName(),

                snapshot.getWorkerName(),

                snapshot.getWorkerAbn(),

                snapshot.getWorkerGstRegistered(),

                snapshot.getGstApplied(),

                snapshot.getRegularRate(),

                snapshot.getOvertime15Rate(),

                snapshot.getOvertime20Rate(),

                snapshot.getSaturdayRate(),

                snapshot.getSundayRate(),

                snapshot.getPublicHolidayRate(),

                snapshot.getTravelRate(),

                snapshot.getKilometreRate(),

                snapshot.getLafhaRate(),

                snapshot.getRegularAmount(),

                snapshot.getOvertime15Amount(),

                snapshot.getOvertime20Amount(),

                snapshot.getSaturdayAmount(),

                snapshot.getSundayAmount(),

                snapshot.getPublicHolidayAmount(),

                snapshot.getTravelAmount(),

                snapshot.getKilometreAmount(),

                snapshot.getLafhaAmount(),

                snapshot.getSubtotalAmount(),

                snapshot.getGstAmount(),

                snapshot.getTotalAmount()
        );
    }
}
