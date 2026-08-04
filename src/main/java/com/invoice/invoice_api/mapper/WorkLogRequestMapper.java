package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTime;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTravel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WorkLogRequestMapper {
    public void apply(
            WorkLog workLog,
            WorkLogRequestDTO request,
            WorkerProfile workerProfile,
            ProjectPosition projectPosition
    ) {

        workLog.setWorkerProfile(workerProfile);

        workLog.setProjectPosition(projectPosition);

        workLog.setWorkDate(request.workDate());

        workLog.setRegularHours(
                normalize(request.regularHours())
        );

        workLog.setOvertime15Hours(
                normalize(request.overtime15Hours())
        );

        workLog.setOvertime20Hours(
                normalize(request.overtime20Hours())
        );

        workLog.setSaturdayHours(
                normalize(request.saturdayHours())
        );

        workLog.setSundayHours(
                normalize(request.sundayHours())
        );

        workLog.setPublicHolidayHours(
                normalize(request.publicHolidayHours())
        );

        workLog.setNotes(
                normalizeText(request.notes())
        );

        workLog.setWorkTime(
                buildWorkTime(request)
        );

        workLog.setTravel(
                buildTravel(request)
        );
    }

    private WorkLogTime buildWorkTime(
            WorkLogRequestDTO request
    ) {

        if (request.workTime() == null) {
            return null;
        }

        WorkLogTime workTime =
                new WorkLogTime();

        workTime.setStartTime(
                request.workTime().startTime()
        );

        workTime.setFinishTime(
                request.workTime().finishTime()
        );

        workTime.setUnpaidBreakMinutes(
                request.workTime().unpaidBreakMinutes()
        );

        return workTime;
    }

    private WorkLogTravel buildTravel(
            WorkLogRequestDTO request
    ) {

        if (request.travel() == null) {
            return null;
        }

        WorkLogTravel travel =
                new WorkLogTravel();

        travel.setTravelHours(
                normalize(
                        request.travel().travelHours()
                )
        );

        travel.setKilometres(
                normalize(
                        request.travel().kilometres()
                )
        );

        travel.setLafhaNights(
                request.travel().lafhaNights()
        );

        return travel;
    }

    private BigDecimal normalize(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String normalizeText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
