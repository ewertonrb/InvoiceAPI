package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
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
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProjectPositionRepository projectPositionRepository;
    private final CompanyContext companyContext;
    private final WorkLogRequestMapper workLogRequestMapper;
    private final WorkLogValidator workLogValidator;
    private final WorkLogFinancialSnapshotBuilder financialSnapshotBuilder;

    public WorkLogService(
            WorkLogRepository workLogRepository,
            WorkerProfileRepository workerProfileRepository,
            CompanyMembershipRepository membershipRepository,
            AuthenticatedUserService authenticatedUserService,
            ProjectPositionRepository projectPositionRepository,
            CompanyContext companyContext,
            WorkLogRequestMapper workLogRequestMapper,
            WorkLogValidator workLogValidator,
            WorkLogFinancialSnapshotBuilder financialSnapshotBuilder
    ) {
        this.workLogRepository = workLogRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.membershipRepository = membershipRepository;
        this.authenticatedUserService = authenticatedUserService;
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
                findWorkerProfileInCompany(
                        request.workerProfileId(),
                        companyId
                );
        requireWorkerSelf(workerProfile);
        requireActiveSelfMembership(workerProfile, companyId);

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

        workLogValidator.validateNoActiveDuplicate(
                workLogRepository.findAllByWorkerProfileIdAndProjectPositionIdAndWorkDateAndStatusNot(
                        workerProfile.getId(), projectPosition.getId(), request.workDate(), WorkLogStatus.CANCELLED),
                request.workTime()
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

        authorizeRead(workLog.getWorkerProfile());

        return WorkLogMapper.toResponseDTO(
                workLog
        );
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDTO> findAll() {
        Long companyId = getCurrentCompanyId();
        requireReviewReader();

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
        requireReviewReader();

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

        authorizeWorkerQuery(workerProfileId);

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

        authorizeWorkerQuery(workerProfileId);

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
        requireReviewReader();

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
        requireReviewReader();

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

        authorizeWorkerQuery(workerProfileId);

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

        authorizeWorkerQuery(workerProfileId);

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
                findWorkerProfileInCompany(
                        request.workerProfileId(),
                        companyId
                );
        requireWorkerSelf(workerProfile);
        requireActiveSelfMembership(workerProfile, companyId);

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

        workLogValidator.validateNoActiveDuplicate(
                workLogRepository.findAllByWorkerProfileIdAndProjectPositionIdAndWorkDateAndStatusNotAndIdNot(
                        workerProfile.getId(), projectPosition.getId(), request.workDate(), WorkLogStatus.CANCELLED, id),
                request.workTime()
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
        requireReviewer();

        WorkLog workLog =
                findWorkLog(
                        workLogId,
                        companyId
                );

        workLogValidator.validateCanBeApproved(
                workLog
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
        requireReviewer();

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
        authorizeMutation(workLog.getWorkerProfile());

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
        authorizeMutation(workLog.getWorkerProfile());

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

    private WorkerProfile findWorkerProfileInCompany(
            Long workerProfileId,
            Long companyId
    ) {
        WorkerProfile profile = findWorkerProfile(workerProfileId);
        membershipRepository
                .findByAppUserIdAndCompanyId(
                        profile.getAppUser().getId(),
                        companyId
                )
                .filter(membership ->
                        (membership.getRole() == CompanyRole.WORKER
                                || membership.getRole() == CompanyRole.OWNER
                                || membership.getRole() == CompanyRole.ADMIN
                                || membership.getRole() == CompanyRole.MANAGER
                                || membership.getRole() == CompanyRole.FINANCE)
                                && (membership.getStatus()
                                == MembershipStatus.ACTIVE
                                || membership.getStatus()
                                == MembershipStatus.SUSPENDED)
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Worker profile not found in this company"
                ));
        return profile;
    }

    private void requireWorkerSelf(WorkerProfile profile) {
        if (companyContext.getRole() == null
                || !isSelfWorkLogRole(companyContext.getRole())
                || !profile.getAppUser().getId().equals(
                authenticatedUserService.getCurrentUserId()
        )) {
            throw new AccessDeniedBusinessException(
                    "Workers can only manage their own work logs"
            );
        }
    }

    private boolean isSelfWorkLogRole(CompanyRole role) {
        return role == CompanyRole.WORKER || role == CompanyRole.OWNER || role == CompanyRole.ADMIN
                || role == CompanyRole.MANAGER || role == CompanyRole.FINANCE;
    }

    private void requireActiveSelfMembership(WorkerProfile profile, Long companyId) {
        membershipRepository.findByAppUserIdAndCompanyId(profile.getAppUser().getId(), companyId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedBusinessException("An active company membership is required to submit work logs."));
    }

    private void authorizeWorkerQuery(Long workerProfileId) {
        WorkerProfile profile = findWorkerProfileInCompany(
                workerProfileId,
                getCurrentCompanyId()
        );
        if (companyContext.getRole() == CompanyRole.WORKER) {
            requireWorkerSelf(profile);
        } else {
            requireReviewReader();
        }
    }

    private void authorizeRead(WorkerProfile profile) {
        if (companyContext.getRole() == CompanyRole.WORKER) {
            requireWorkerSelf(profile);
        } else {
            requireReviewReader();
        }
    }

    private void authorizeMutation(WorkerProfile profile) {
        if (companyContext.getRole() == CompanyRole.WORKER) {
            requireWorkerSelf(profile);
        } else {
            requireReviewer();
        }
    }

    private void requireReviewReader() {
        CompanyRole role = companyContext.getRole();
        if (role != CompanyRole.OWNER
                && role != CompanyRole.ADMIN
                && role != CompanyRole.MANAGER
                && role != CompanyRole.FINANCE) {
            throw new AccessDeniedBusinessException(
                    "This work-log view is restricted to company reviewers"
            );
        }
    }

    private void requireReviewer() {
        requireReviewReader();
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
