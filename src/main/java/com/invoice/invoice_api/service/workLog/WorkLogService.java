package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.WorkLogMapper;
import com.invoice.invoice_api.mapper.WorkLogRequestMapper;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final CompanyContext companyContext;
    private final WorkLogRequestMapper workLogRequestMapper;
    private final WorkLogValidator workLogValidator;
    private final WorkLogFinancialSnapshotBuilder financialSnapshotBuilder;

    public WorkLogService(
            WorkLogRepository workLogRepository,
            WorkerProfileRepository workerProfileRepository,
            ProjectPositionRepository projectPositionRepository,
            CompanyContext companyContext,
            WorkLogRequestMapper workLogRequestMapper,
            WorkLogValidator workLogValidator,
            WorkLogFinancialSnapshotBuilder financialSnapshotBuilder
    ) {
        this.workLogRepository = workLogRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.projectPositionRepository = projectPositionRepository;
        this.companyContext = companyContext;
        this.workLogRequestMapper = workLogRequestMapper;
        this.workLogValidator = workLogValidator;
        this.financialSnapshotBuilder =
                financialSnapshotBuilder;
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

        workLogValidator.validateRequest(request);

        WorkerProfile workerProfile =
                findWorkerProfile(
                        request.workerProfileId()
                );

        ProjectPosition projectPosition =
                findProjectPosition(
                        request.projectPositionId(),
                        companyId
                );

        workLogValidator.validateWorkerCanUseWorkLogs(
                workerProfile
        );

        workLogValidator.validateProjectPositionActive(
                projectPosition
        );

        workLogValidator.validateActiveRateExists(
                projectPosition.getId(),
                request.workDate()
        );

        WorkLog workLog = new WorkLog();

        workLogRequestMapper.apply(
                workLog,
                request,
                workerProfile,
                projectPosition
        );

        WorkLogRules.submit(workLog);

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
                findWorkLog(
                        id,
                        companyId
                );

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
    public List<WorkLogResponseDTO> findAllByStatus(
            WorkLogStatus status
    ) {
        Long companyId = getCurrentCompanyId();

        return workLogRepository
                .findAllByProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
                        companyId,
                        status
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
    public List<WorkLogResponseDTO> findByWorkerAndStatus(
            Long workerProfileId,
            WorkLogStatus status
    ) {
        Long companyId = getCurrentCompanyId();

        findWorkerProfile(workerProfileId);

        return workLogRepository
                .findAllByWorkerProfileIdAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
                        workerProfileId,
                        companyId,
                        status
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
    public List<WorkLogResponseDTO> findByProjectAndStatus(
            Long projectId,
            WorkLogStatus status
    ) {
        Long companyId = getCurrentCompanyId();

        return workLogRepository
                .findAllByProjectPositionProjectIdAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
                        projectId,
                        companyId,
                        status
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

        workLogValidator.validatePeriod(
                startDate,
                endDate
        );

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

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findByWorkerPeriodAndStatus(
            Long workerProfileId,
            LocalDate startDate,
            LocalDate endDate,
            WorkLogStatus status
    ) {
        Long companyId = getCurrentCompanyId();

        workLogValidator.validatePeriod(
                startDate,
                endDate
        );

        findWorkerProfile(workerProfileId);

        return workLogRepository
                .findAllByWorkerProfileIdAndWorkDateBetweenAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateAsc(
                        workerProfileId,
                        startDate,
                        endDate,
                        companyId,
                        status
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

        workLogValidator.validateRequest(request);

        WorkLog workLog =
                findWorkLog(
                        id,
                        companyId
                );

        workLogValidator.validateCanBeEdited(
                workLog
        );

        WorkerProfile workerProfile =
                findWorkerProfile(
                        request.workerProfileId()
                );

        ProjectPosition projectPosition =
                findProjectPosition(
                        request.projectPositionId(),
                        companyId
                );

        workLogValidator.validateWorkerCanUseWorkLogs(
                workerProfile
        );

        workLogValidator.validateProjectPositionActive(
                projectPosition
        );

        workLogValidator.validateActiveRateExists(
                projectPosition.getId(),
                request.workDate()
        );

        workLogRequestMapper.apply(
                workLog,
                request,
                workerProfile,
                projectPosition
        );

        /*
         * Any update represents a new submission for approval.
         * Previous approval, rejection, and snapshot information
         * must therefore be cleared.
         */
        WorkLogRules.submit(workLog);

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

        workLogValidator.validateCanBeApproved(
                workLog
        );

        workLogValidator.validateWorkerCanUseWorkLogs(
                workLog.getWorkerProfile()
        );

        workLogValidator.validateProjectPositionActive(
                workLog.getProjectPosition()
        );

        workLogValidator.validateActiveRateExists(
                workLog.getProjectPosition().getId(),
                workLog.getWorkDate()
        );

        WorkLogFinancialSnapshot snapshot =
                financialSnapshotBuilder.build(
                        workLog
                );

        workLog.setFinancialSnapshot(
                snapshot
        );

        WorkLogRules.approve(
                workLog
        );

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

        workLogValidator.validateCanBeRejected(
                workLog
        );

        String normalizedReason =
                workLogValidator
                        .normalizeAndValidateRejectionReason(
                                rejectionReason
                        );

        WorkLogRules.reject(
                workLog,
                normalizedReason
        );

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

        workLogValidator.validateCanBeCancelled(
                workLog
        );

        WorkLogRules.cancel(workLog);

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

        workLogValidator.validateCanBeReopened(
                workLog
        );

        workLogValidator.validateWorkerCanUseWorkLogs(
                workLog.getWorkerProfile()
        );

        workLogValidator.validateProjectPositionActive(
                workLog.getProjectPosition()
        );

        workLogValidator.validateActiveRateExists(
                workLog.getProjectPosition().getId(),
                workLog.getWorkDate()
        );

        WorkLogRules.reopen(workLog);

        WorkLog reopenedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                reopenedWorkLog
        );
    }

    /*
     * This operation will later be called internally by the
     * InvoiceService. It should not be directly exposed to workers.
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

        workLogValidator.validateCanBeInvoiced(
                workLog
        );

        WorkLogRules.markAsInvoiced(
                workLog
        );

        WorkLog invoicedWorkLog =
                workLogRepository.save(workLog);

        return WorkLogMapper.toResponseDTO(
                invoicedWorkLog
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project position not found with ID: "
                                        + projectPositionId
                        )
                );
    }

    /*
     * ============================================================
     * COMPANY CONTEXT
     * ============================================================
     */

    private Long getCurrentCompanyId() {
        return companyContext.getCompanyId();
    }
}
