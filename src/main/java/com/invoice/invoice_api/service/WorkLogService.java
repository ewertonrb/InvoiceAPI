package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.WorkLogMapper;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.ProjectRoleRateRepository;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkLogService {
    private final WorkLogRepository workLogRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final ProjectRoleRateRepository projectRoleRateRepository;
    private final CompanyContext companyContext;

    public WorkLogService(
            WorkLogRepository workLogRepository,
            WorkerProfileRepository workerProfileRepository,
            ProjectPositionRepository projectPositionRepository,
            ProjectRoleRateRepository projectRoleRateRepository,
            CompanyContext companyContext
    ) {
        this.workLogRepository = workLogRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.projectPositionRepository = projectPositionRepository;
        this.projectRoleRateRepository = projectRoleRateRepository;
        this.companyContext = companyContext;
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */

    @Transactional
    public WorkLogResponseDTO create(
            WorkLogRequestDTO request
    ) {
        Long companyId = getCurrentCompanyId();

        validateRequest(request);

        WorkerProfile workerProfile =
                findWorkerProfile(
                        request.workerProfileId()
                );

        ProjectPosition projectPosition =
                findProjectPosition(
                        request.projectPositionId(),
                        companyId
                );

        validateWorkerActive(workerProfile);
        validateProjectPositionActive(projectPosition);

        validateActiveRateExists(
                projectPosition.getId(),
                request.workDate()
        );

        WorkLog workLog = new WorkLog();

        applyRequest(
                workLog,
                request,
                workerProfile,
                projectPosition
        );

        workLog.setStatus(
                WorkLogStatus.PENDING_APPROVAL
        );

        workLog.setSubmittedAt(
                LocalDateTime.now()
        );

        workLog.setApprovedAt(null);
        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);

        WorkLog savedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                savedWorkLog
        );
    }

    /*
     * ============================================================
     * READ
     * ============================================================
     */

    @Transactional(readOnly = true)
    public WorkLogResponseDTO findById(
            Long id
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(id, companyId);

        return WorkLogMapper.toResponseDTO(
                workLog
        );
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findAll() {
        Long companyId = getCurrentCompanyId();

        return workLogRepository
                .findAllByProjectPositionProjectCompanyIdOrderByWorkDateDesc(
                        companyId
                )
                .stream()
                .map(WorkLogMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findByWorker(
            Long workerProfileId
    ) {
        Long companyId = getCurrentCompanyId();

        findWorkerProfile(workerProfileId);

        return workLogRepository
                .findAllByWorkerProfileIdAndProjectPositionProjectCompanyIdOrderByWorkDateDesc(
                        workerProfileId,
                        companyId
                )
                .stream()
                .map(WorkLogMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findByProject(
            Long projectId
    ) {
        Long companyId = getCurrentCompanyId();

        return workLogRepository
                .findAllByProjectPositionProjectIdAndProjectPositionProjectCompanyIdOrderByWorkDateDesc(
                        projectId,
                        companyId
                )
                .stream()
                .map(WorkLogMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findByWorkerAndPeriod(
            Long workerProfileId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long companyId = getCurrentCompanyId();

        validatePeriod(startDate, endDate);
        findWorkerProfile(workerProfileId);

        return workLogRepository
                .findAllByWorkerProfileIdAndWorkDateBetweenAndProjectPositionProjectCompanyIdOrderByWorkDateAsc(
                        workerProfileId,
                        startDate,
                        endDate,
                        companyId
                )
                .stream()
                .map(WorkLogMapper::toResponseDTO)
                .toList();
    }

    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */

    @Transactional
    public WorkLogResponseDTO update(
            Long id,
            WorkLogRequestDTO request
    ) {
        Long companyId = getCurrentCompanyId();

        validateRequest(request);

        WorkLog workLog =
                findWorkLog(id, companyId);

        validateWorkLogCanBeEdited(workLog);

        WorkerProfile workerProfile =
                findWorkerProfile(
                        request.workerProfileId()
                );

        ProjectPosition projectPosition =
                findProjectPosition(
                        request.projectPositionId(),
                        companyId
                );

        validateWorkerActive(workerProfile);
        validateProjectPositionActive(projectPosition);

        validateActiveRateExists(
                projectPosition.getId(),
                request.workDate()
        );

        applyRequest(
                workLog,
                request,
                workerProfile,
                projectPosition
        );

        /*
         * Toda alteração feita pelo worker representa uma nova
         * submissão para análise.
         */
        workLog.setStatus(
                WorkLogStatus.PENDING_APPROVAL
        );

        workLog.setSubmittedAt(
                LocalDateTime.now()
        );

        workLog.setApprovedAt(null);
        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);

        WorkLog updatedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                updatedWorkLog
        );
    }

    /*
     * ============================================================
     * APPROVAL WORKFLOW
     * ============================================================
     */

    @Transactional
    public WorkLogResponseDTO approve(
            Long workLogId
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        validateWorkLogActive(workLog);

        validateCurrentStatus(
                workLog,
                WorkLogStatus.PENDING_APPROVAL,
                "Only pending work logs can be approved."
        );

        validateWorkerActive(
                workLog.getWorkerProfile()
        );

        validateProjectPositionActive(
                workLog.getProjectPosition()
        );

        validateActiveRateExists(
                workLog.getProjectPosition().getId(),
                workLog.getWorkDate()
        );

        workLog.setStatus(
                WorkLogStatus.APPROVED
        );

        workLog.setApprovedAt(
                LocalDateTime.now()
        );

        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);

        WorkLog approvedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                approvedWorkLog
        );
    }

    @Transactional
    public WorkLogResponseDTO reject(
            Long workLogId,
            String rejectionReason
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        validateWorkLogActive(workLog);

        validateCurrentStatus(
                workLog,
                WorkLogStatus.PENDING_APPROVAL,
                "Only pending work logs can be rejected."
        );

        String normalizedReason =
                normalizeRequiredText(
                        rejectionReason,
                        "Rejection reason is required."
                );

        if (normalizedReason.length() > 500) {
            throw new BusinessException(
                    "Rejection reason cannot exceed 500 characters."
            );
        }

        workLog.setStatus(
                WorkLogStatus.REJECTED
        );

        workLog.setRejectedAt(
                LocalDateTime.now()
        );

        workLog.setRejectionReason(
                normalizedReason
        );

        workLog.setApprovedAt(null);

        WorkLog rejectedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                rejectedWorkLog
        );
    }

    @Transactional
    public WorkLogResponseDTO cancel(
            Long workLogId
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        if (
                workLog.getStatus()
                        == WorkLogStatus.INVOICED
        ) {
            throw new BusinessException(
                    "An invoiced work log cannot be cancelled directly."
            );
        }

        if (
                workLog.getStatus()
                        == WorkLogStatus.CANCELLED
        ) {
            throw new BusinessException(
                    "Work log is already cancelled."
            );
        }

        workLog.setStatus(
                WorkLogStatus.CANCELLED
        );

        workLog.setApprovedAt(null);
        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);

        WorkLog cancelledWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                cancelledWorkLog
        );
    }

    @Transactional
    public WorkLogResponseDTO reopen(
            Long workLogId
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        if (
                workLog.getStatus()
                        == WorkLogStatus.PENDING_APPROVAL
        ) {
            throw new BusinessException(
                    "Work log is already pending approval."
            );
        }

        if (
                workLog.getStatus()
                        == WorkLogStatus.INVOICED
        ) {
            throw new BusinessException(
                    "An invoiced work log cannot be reopened directly."
            );
        }

        validateWorkLogActive(workLog);

        workLog.setStatus(
                WorkLogStatus.PENDING_APPROVAL
        );

        workLog.setSubmittedAt(
                LocalDateTime.now()
        );

        workLog.setApprovedAt(null);
        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);

        WorkLog reopenedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                reopenedWorkLog
        );
    }

    /*
     * Este método será utilizado futuramente pelo InvoiceService.
     * Ele não deve ser exposto diretamente para o worker.
     */
    @Transactional
    public WorkLogResponseDTO markAsInvoiced(
            Long workLogId
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        validateWorkLogActive(workLog);

        validateCurrentStatus(
                workLog,
                WorkLogStatus.APPROVED,
                "Only approved work logs can be invoiced."
        );

        workLog.setStatus(
                WorkLogStatus.INVOICED
        );

        WorkLog invoicedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                invoicedWorkLog
        );
    }

    /*
     * ============================================================
     * ACTIVE / INACTIVE
     * ============================================================
     */

    @Transactional
    public WorkLogResponseDTO deactivate(
            Long id
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(id, companyId);

        if (
                Boolean.FALSE.equals(
                        workLog.getActive()
                )
        ) {
            throw new BusinessException(
                    "Work log is already inactive."
            );
        }

        if (
                workLog.getStatus()
                        == WorkLogStatus.INVOICED
        ) {
            throw new BusinessException(
                    "An invoiced work log cannot be deactivated."
            );
        }

        workLog.setActive(false);

        WorkLog updatedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                updatedWorkLog
        );
    }

    @Transactional
    public WorkLogResponseDTO reactivate(
            Long id
    ) {
        Long companyId = getCurrentCompanyId();

        WorkLog workLog =
                findWorkLog(id, companyId);

        if (
                Boolean.TRUE.equals(
                        workLog.getActive()
                )
        ) {
            throw new BusinessException(
                    "Work log is already active."
            );
        }

        validateWorkerActive(
                workLog.getWorkerProfile()
        );

        validateProjectPositionActive(
                workLog.getProjectPosition()
        );

        validateActiveRateExists(
                workLog.getProjectPosition().getId(),
                workLog.getWorkDate()
        );

        workLog.setActive(true);

        /*
         * Caso tenha sido desativado antes da aprovação,
         * retorna para a fila de aprovação.
         */
        if (
                workLog.getStatus()
                        != WorkLogStatus.INVOICED
        ) {
            workLog.setStatus(
                    WorkLogStatus.PENDING_APPROVAL
            );

            workLog.setSubmittedAt(
                    LocalDateTime.now()
            );

            workLog.setApprovedAt(null);
            workLog.setRejectedAt(null);
            workLog.setRejectionReason(null);
        }

        WorkLog updatedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                updatedWorkLog
        );
    }

    /*
     * ============================================================
     * REQUEST MAPPING
     * ============================================================
     */

    private void applyRequest(
            WorkLog workLog,
            WorkLogRequestDTO request,
            WorkerProfile workerProfile,
            ProjectPosition projectPosition
    ) {
        workLog.setWorkerProfile(
                workerProfile
        );

        workLog.setProjectPosition(
                projectPosition
        );

        workLog.setWorkDate(
                request.workDate()
        );

        workLog.setRegularHours(
                normalizeQuantity(
                        request.regularHours()
                )
        );

        workLog.setOvertime15Hours(
                normalizeQuantity(
                        request.overtime15Hours()
                )
        );

        workLog.setOvertime20Hours(
                normalizeQuantity(
                        request.overtime20Hours()
                )
        );

        workLog.setSaturdayHours(
                normalizeQuantity(
                        request.saturdayHours()
                )
        );

        workLog.setSundayHours(
                normalizeQuantity(
                        request.sundayHours()
                )
        );

        workLog.setPublicHolidayHours(
                normalizeQuantity(
                        request.publicHolidayHours()
                )
        );

        workLog.setTravelHours(
                normalizeQuantity(
                        request.travelHours()
                )
        );

        workLog.setKilometres(
                normalizeQuantity(
                        request.kilometres()
                )
        );

        workLog.setLafhaNights(
                request.lafhaNights() == null
                        ? 0
                        : request.lafhaNights()
        );

        workLog.setNotes(
                normalizeOptionalText(
                        request.notes()
                )
        );
    }

    /*
     * ============================================================
     * ENTITY LOOKUPS
     * ============================================================
     */

    private WorkLog findWorkLog(
            Long workLogId,
            Long companyId
    ) {
        return workLogRepository
                .findByIdAndProjectPositionProjectCompanyId(
                        workLogId,
                        companyId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Work log not found with ID: "
                                        + workLogId
                        )
                );
    }

    private WorkerProfile findWorkerProfile(
            Long workerProfileId
    ) {
        return workerProfileRepository
                .findById(workerProfileId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Worker profile not found with ID: "
                                        + workerProfileId
                        )
                );
    }

    private ProjectPosition findProjectPosition(
            Long projectPositionId,
            Long companyId
    ) {
        return projectPositionRepository
                .findByIdAndProjectCompanyId(
                        projectPositionId,
                        companyId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Project position not found with ID: "
                                        + projectPositionId
                        )
                );
    }

    /*
     * ============================================================
     * BUSINESS VALIDATIONS
     * ============================================================
     */

    private void validateWorkLogCanBeEdited(
            WorkLog workLog
    ) {
        validateWorkLogActive(workLog);

        if (
                workLog.getStatus()
                        != WorkLogStatus.PENDING_APPROVAL
                        && workLog.getStatus()
                        != WorkLogStatus.REJECTED
        ) {
            throw new BusinessException(
                    "Only pending or rejected work logs can be edited."
            );
        }
    }

    private void validateCurrentStatus(
            WorkLog workLog,
            WorkLogStatus expectedStatus,
            String errorMessage
    ) {
        if (
                workLog.getStatus()
                        != expectedStatus
        ) {
            throw new BusinessException(
                    errorMessage
            );
        }
    }

    private void validateWorkLogActive(
            WorkLog workLog
    ) {
        if (
                !Boolean.TRUE.equals(
                        workLog.getActive()
                )
        ) {
            throw new BusinessException(
                    "Work log is inactive."
            );
        }
    }

    private void validateActiveRateExists(
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

    private void validateRequest(
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

        validateNonNegative(
                request.travelHours(),
                "Travel hours"
        );

        validateNonNegative(
                request.kilometres(),
                "Kilometres"
        );

        if (
                request.lafhaNights() != null
                        && request.lafhaNights() < 0
        ) {
            throw new BusinessException(
                    "LAFHA nights cannot be negative."
            );
        }

        validateAtLeastOneQuantity(request);
    }

    private void validateAtLeastOneQuantity(
            WorkLogRequestDTO request
    ) {
        boolean hasHours =
                isPositive(request.regularHours())
                        || isPositive(request.overtime15Hours())
                        || isPositive(request.overtime20Hours())
                        || isPositive(request.saturdayHours())
                        || isPositive(request.sundayHours())
                        || isPositive(
                        request.publicHolidayHours()
                )
                        || isPositive(request.travelHours());

        boolean hasKilometres =
                isPositive(
                        request.kilometres()
                );

        boolean hasLafha =
                request.lafhaNights() != null
                        && request.lafhaNights() > 0;

        if (
                !hasHours
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

    private void validateWorkerActive(
            WorkerProfile workerProfile
    ) {
        if (
                !Boolean.TRUE.equals(
                        workerProfile.getActive()
                )
        ) {
            throw new BusinessException(
                    "Worker profile is inactive."
            );
        }
    }

    private void validateProjectPositionActive(
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

    private void validatePeriod(
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

        if (
                startDate.isAfter(endDate)
        ) {
            throw new BusinessException(
                    "Start date cannot be after end date."
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

        if (
                workDate.isAfter(
                        LocalDate.now()
                )
        ) {
            throw new BusinessException(
                    "Work date cannot be in the future."
            );
        }
    }

    /*
     * ============================================================
     * NORMALIZATION
     * ============================================================
     */

    private boolean isPositive(
            BigDecimal value
    ) {
        return value != null
                && value.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    private BigDecimal normalizeQuantity(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private String normalizeRequiredText(
            String value,
            String errorMessage
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new BusinessException(
                    errorMessage
            );
        }

        return value.trim();
    }

    private Long getCurrentCompanyId() {
        return companyContext.getCompanyId();
    }
}
