package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogTimeRequestDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTime;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.repository.ProjectRoleRateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class WorkLogValidator {
    private final ProjectRoleRateRepository projectRoleRateRepository;

    public WorkLogValidator(
            ProjectRoleRateRepository projectRoleRateRepository
    ) {
        this.projectRoleRateRepository =
                projectRoleRateRepository;
    }

    /*
     * ============================================================
     * REQUEST
     * ============================================================
     */

    public void validateRequest(
            WorkLogRequestDTO request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Work log request is required."
            );
        }

        validateNotFutureDate(
                request.workDate()
        );

        validateNonNegative(
                request.regularHours(),
                "Regular hours"
        );

        validateNonNegative(
                request.overtime15Hours(),
                "Overtime 1.5 hours"
        );

        validateNonNegative(
                request.overtime20Hours(),
                "Overtime 2.0 hours"
        );

        validateNonNegative(
                request.saturdayHours(),
                "Saturday hours"
        );

        validateNonNegative(
                request.sundayHours(),
                "Sunday hours"
        );

        validateNonNegative(
                request.publicHolidayHours(),
                "Public holiday hours"
        );

        validateWorkTime(
                request
        );

        validateTravel(
                request
        );

        validateAtLeastOneQuantity(
                request
        );
    }

    /*
     * ============================================================
     * WORKFLOW
     * ============================================================
     */

    public void validateCanBeEdited(
            WorkLog workLog
    ) {
        if (!workLog.canBeEdited()) {
            throw new BusinessException(
                    "Only pending or rejected work logs can be edited."
            );
        }
    }

    public void validateNoActiveDuplicate(
            List<WorkLog> existingWorkLogs,
            WorkLogTimeRequestDTO requestedTime
    ) {
        if (existingWorkLogs == null || existingWorkLogs.isEmpty()) {
            return;
        }

        if (!hasCompleteTime(requestedTime)) {
            throw overlappingWorkLogException();
        }

        for (WorkLog existingWorkLog : existingWorkLogs) {
            if (!hasCompleteTime(existingWorkLog.getWorkTime())
                    || overlaps(existingWorkLog.getWorkTime(), requestedTime)) {
                throw overlappingWorkLogException();
            }
        }
    }

    private boolean hasCompleteTime(WorkLogTimeRequestDTO time) {
        return time != null && time.startTime() != null && time.finishTime() != null;
    }

    private boolean hasCompleteTime(WorkLogTime time) {
        return time != null && time.hasStartAndFinishTime();
    }

    private boolean overlaps(WorkLogTime existing, WorkLogTimeRequestDTO requested) {
        return intervals(existing.getStartTime(), existing.getFinishTime())
                .stream()
                .anyMatch(existingInterval -> intervals(requested.startTime(), requested.finishTime())
                        .stream()
                        .anyMatch(requestedInterval -> existingInterval.overlaps(requestedInterval)));
    }

    private List<Interval> intervals(LocalTime start, LocalTime finish) {
        int startMinute = start.getHour() * 60 + start.getMinute();
        int finishMinute = finish.getHour() * 60 + finish.getMinute();
        if (finishMinute > startMinute) {
            return List.of(new Interval(startMinute, finishMinute));
        }
        if (finishMinute < startMinute) {
            return List.of(new Interval(startMinute, 24 * 60), new Interval(0, finishMinute));
        }
        return List.of();
    }

    private BusinessException overlappingWorkLogException() {
        return new BusinessException("A work log already exists for this worker, project position, and overlapping time.");
    }

    private record Interval(int start, int finish) {
        private boolean overlaps(Interval other) {
            return start < other.finish && other.start < finish;
        }
    }

    public void validateCanBeApproved(
            WorkLog workLog
    ) {
        if (
                workLog.getStatus()
                        != WorkLogStatus.PENDING_APPROVAL
        ) {
            throw new BusinessException(
                    "Only pending work logs can be approved."
            );
        }
    }

    public void validateCanBeRejected(
            WorkLog workLog
    ) {
        if (
                workLog.getStatus()
                        != WorkLogStatus.PENDING_APPROVAL
        ) {
            throw new BusinessException(
                    "Only pending work logs can be rejected."
            );
        }
    }

    public void validateCanBeCancelled(
            WorkLog workLog
    ) {
        if (workLog.isCancelled()) {
            throw new BusinessException(
                    "Work log is already cancelled."
            );
        }

        if (workLog.isInvoiced()) {
            throw new BusinessException(
                    "An invoiced work log cannot be cancelled directly."
            );
        }
    }

    public void validateCanBeReopened(
            WorkLog workLog
    ) {
        if (workLog.isApproved()) {
            throw new BusinessException(
                    "An approved work log cannot be reopened after snapshot creation."
            );
        }

        if (workLog.isPendingApproval()) {
            throw new BusinessException(
                    "Work log is already pending approval."
            );
        }

        if (workLog.isInvoiced()) {
            throw new BusinessException(
                    "An invoiced work log cannot be reopened directly."
            );
        }

        if (workLog.isCancelled()) {
            throw new BusinessException(
                    "A cancelled work log cannot be reopened."
            );
        }
    }

    public void validateCanBeInvoiced(
            WorkLog workLog
    ) {
        if (!workLog.isApproved()) {
            throw new BusinessException(
                    "Only approved work logs can be invoiced."
            );
        }

        if (!workLog.hasFinancialSnapshot()) {
            throw new BusinessException(
                    "Approved work log does not have a financial snapshot."
            );
        }
    }

    /*
     * ============================================================
     * RELATED ENTITIES
     * ============================================================
     */

    public void validateWorkerCanUseWorkLogs(
            WorkerProfile workerProfile
    ) {
        if (!workerProfile.isComplete()) {
            throw new BusinessException(
                    "Worker profile must be complete before using work logs."
            );
        }
    }

    public void validateProjectPositionActive(
            ProjectPosition projectPosition
    ) {
        if (
                !Boolean.TRUE.equals(
                        projectPosition.getActive()
                )
        ) {
            throw new BusinessException(
                    "Project position is inactive."
            );
        }
    }

    public void validateActiveRateExists(
            Long projectPositionId,
            LocalDate workDate
    ) {
        boolean rateExists =
                projectRoleRateRepository
                        .existsActiveRateForDate(
                                projectPositionId,
                                workDate
                        );

        if (!rateExists) {
            throw new BusinessException(
                    "No active rate was found for project position "
                            + projectPositionId
                            + " on "
                            + workDate
                            + "."
            );
        }
    }

    /*
     * ============================================================
     * PERIOD
     * ============================================================
     */

    public void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (
                startDate == null
                        || endDate == null
        ) {
            throw new BusinessException(
                    "Start date and end date are required."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Start date cannot be after end date."
            );
        }
    }

    /*
     * ============================================================
     * REJECTION
     * ============================================================
     */

    public String normalizeAndValidateRejectionReason(
            String rejectionReason
    ) {
        if (
                rejectionReason == null
                        || rejectionReason.isBlank()
        ) {
            throw new BusinessException(
                    "Rejection reason is required."
            );
        }

        String normalizedReason =
                rejectionReason.trim();

        if (normalizedReason.length() > 500) {
            throw new BusinessException(
                    "Rejection reason cannot exceed 500 characters."
            );
        }

        return normalizedReason;
    }

    /*
     * ============================================================
     * PRIVATE REQUEST VALIDATIONS
     * ============================================================
     */

    private void validateWorkTime(
            WorkLogRequestDTO request
    ) {
        if (request.workTime() == null) {
            return;
        }

        Integer breakMinutes =
                request.workTime()
                        .unpaidBreakMinutes();

        if (
                breakMinutes != null
                        && breakMinutes < 0
        ) {
            throw new BusinessException(
                    "Unpaid break minutes cannot be negative."
            );
        }

        LocalTime startTime =
                request.workTime()
                        .startTime();

        LocalTime finishTime =
                request.workTime()
                        .finishTime();

        boolean onlyOneTimeProvided =
                startTime == null
                        && finishTime != null
                        || startTime != null
                        && finishTime == null;

        if (onlyOneTimeProvided) {
            throw new BusinessException(
                    "Start time and finish time must be provided together."
            );
        }
    }

    private void validateTravel(
            WorkLogRequestDTO request
    ) {
        if (request.travel() == null) {
            return;
        }

        validateNonNegative(
                request.travel().travelHours(),
                "Travel hours"
        );

        validateNonNegative(
                request.travel().kilometres(),
                "Kilometres"
        );

        Integer lafhaNights =
                request.travel().lafhaNights();

        if (
                lafhaNights != null
                        && lafhaNights < 0
        ) {
            throw new BusinessException(
                    "LAFHA nights cannot be negative."
            );
        }
    }

    private void validateAtLeastOneQuantity(
            WorkLogRequestDTO request
    ) {
        boolean hasWorkHours =
                isPositive(request.regularHours())
                        || isPositive(request.overtime15Hours())
                        || isPositive(request.overtime20Hours())
                        || isPositive(request.saturdayHours())
                        || isPositive(request.sundayHours())
                        || isPositive(
                        request.publicHolidayHours()
                );

        boolean hasTravelHours =
                request.travel() != null
                        && isPositive(
                        request.travel().travelHours()
                );

        boolean hasKilometres =
                request.travel() != null
                        && isPositive(
                        request.travel().kilometres()
                );

        boolean hasLafha =
                request.travel() != null
                        && request.travel().lafhaNights() != null
                        && request.travel().lafhaNights() > 0;

        if (
                !hasWorkHours
                        && !hasTravelHours
                        && !hasKilometres
                        && !hasLafha
        ) {
            throw new BusinessException(
                    "At least one work quantity must be greater than zero."
            );
        }
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName
    ) {
        if (
                value != null
                        && value.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new BusinessException(
                    fieldName
                            + " cannot be negative."
            );
        }
    }

    private void validateNotFutureDate(
            LocalDate workDate
    ) {
        if (workDate == null) {
            throw new BusinessException(
                    "Work date is required."
            );
        }

        if (workDate.isAfter(LocalDate.now())) {
            throw new BusinessException(
                    "Work date cannot be in the future."
            );
        }
    }

    private boolean isPositive(
            BigDecimal value
    ) {
        return value != null
                && value.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }
}
