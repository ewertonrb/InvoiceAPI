package com.invoice.invoice_api.service;


import com.invoice.invoice_api.dto.workerProfile.WorkerProfileAdminResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileSummaryDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.WorkerProfileStatus;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.BankDetailsMapper;
import com.invoice.invoice_api.mapper.SuperDetailsMapper;
import com.invoice.invoice_api.mapper.WorkerProfileMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.SuperDetails;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final WorkerProfileValidator workerProfileValidator;
    private final CompanyContext companyContext;

    public WorkerProfileService(
            WorkerProfileRepository workerProfileRepository,
            CompanyMembershipRepository membershipRepository,
            AuthenticatedUserService authenticatedUserService,
            WorkerProfileValidator workerProfileValidator,
            CompanyContext companyContext
    ) {
        this.workerProfileRepository = workerProfileRepository;
        this.membershipRepository = membershipRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.workerProfileValidator = workerProfileValidator;
        this.companyContext = companyContext;
    }

    /*
     * ============================================================
     * CURRENT USER
     * ============================================================
     */

    @Transactional(readOnly = true)
    public WorkerProfileResponseDTO findCurrentProfile() {
        validateCurrentUserCanSelfManageProfile();

        WorkerProfile workerProfile =
                findCurrentWorkerProfileEntity();

        return WorkerProfileMapper.toResponseDTO(
                workerProfile
        );
    }

    @Transactional
    public WorkerProfileResponseDTO updateCurrentProfile(
            WorkerProfileRequestDTO request
    ) {
        validateCurrentUserCanSelfManageProfile();
        WorkerProfile workerProfile =
                findCurrentWorkerProfileEntity();

        workerProfileValidator.validateCanBeUpdated(
                workerProfile
        );

        String normalizedAbn =
                workerProfileValidator
                        .normalizeAndValidateAbn(
                                request.abn(),
                                workerProfile.getId()
                        );

        BankDetails bankDetails =
                BankDetailsMapper.toEntity(
                        request.bankDetails()
                );

        SuperDetails superDetails =
                SuperDetailsMapper.toEntity(
                        request.superDetails()
                );

        WorkerProfileRules.applyProfileInformation(
                workerProfile,
                normalizedAbn,
                request.gstRegistered(),
                request.phone(),
                request.notes(),
                bankDetails,
                superDetails
        );

        WorkerProfile savedWorkerProfile =
                workerProfileRepository.save(
                        workerProfile
                );

        return WorkerProfileMapper.toResponseDTO(
                savedWorkerProfile
        );
    }

    /*
     * ============================================================
     * ADMIN QUERIES
     * ============================================================
     */

    @Transactional(readOnly = true)
    public WorkerProfileAdminResponseDTO findById(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                findEntityById(workerProfileId);

        CompanyMembership membership = validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        return WorkerProfileMapper.toAdminResponseDTO(
                workerProfile,
                membership
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerProfileSummaryDTO> findActiveWorkersByCompany(
            Long companyId
    ) {
        return findWorkersByCompany(
                companyId,
                true,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerProfileSummaryDTO> findWorkersByCompany(
            Long companyId,
            boolean activeOnly,
            MembershipStatus status
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        List<MembershipStatus> statuses = status != null
                ? List.of(status)
                : activeOnly
                ? List.of(MembershipStatus.ACTIVE)
                : List.of(
                        MembershipStatus.ACTIVE,
                        MembershipStatus.SUSPENDED
                );

        Map<Long, CompanyMembership> membershipsByUserId =
                membershipRepository
                        .findByCompanyIdAndStatusIn(
                                companyId,
                                statuses
                        )
                        .stream()
                        .filter(membership ->
                                membership.getRole()
                                        == CompanyRole.WORKER
                        )
                        .collect(Collectors.toMap(
                                membership -> membership
                                        .getAppUser()
                                        .getId(),
                                Function.identity()
                        ));

        return workerProfileRepository
                .findWorkersByCompanyIdAndMembershipStatuses(
                        companyId,
                        statuses
                )
                .stream()
                .map(workerProfile -> {
                    CompanyMembership membership =
                            membershipsByUserId.get(
                                    workerProfile
                                            .getAppUser()
                                            .getId()
                            );

                    return WorkerProfileMapper.toSummaryDTO(
                            workerProfile,
                            membership
                    );
                })
                .toList();
    }

    /*
     * ============================================================
     * SUSPEND
     * ============================================================
     */

    @Transactional
    public WorkerProfileAdminResponseDTO suspend(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                lockWorkerProfileById(workerProfileId);

        CompanyMembership workerMembership = validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        if (
                workerMembership.getStatus()
                        == MembershipStatus.SUSPENDED
        ) {
            throw new InvalidOperationException(
                    "Worker membership is already suspended."
            );
        }

        if (workerMembership.getStatus()
                != MembershipStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Only active worker memberships can be suspended."
            );
        }

        workerMembership.setStatus(MembershipStatus.SUSPENDED);
        workerMembership.setSuspendedAt(LocalDateTime.now());
        membershipRepository.save(workerMembership);

        if (!hasAnotherActiveWorkerMembership(
                workerProfile,
                companyId
        )) {
            workerProfile.setStatus(
                    WorkerProfileStatus.SUSPENDED
            );
        }

        WorkerProfile savedWorkerProfile =
                workerProfileRepository.save(
                        workerProfile
                );

        return WorkerProfileMapper.toAdminResponseDTO(
                savedWorkerProfile,
                workerMembership
        );
    }

    /*
     * ============================================================
     * REACTIVATE
     * ============================================================
     */

    @Transactional
    public WorkerProfileAdminResponseDTO reactivate(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                lockWorkerProfileById(workerProfileId);

        CompanyMembership workerMembership = validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        if (
                workerMembership.getStatus()
                        != MembershipStatus.SUSPENDED
        ) {
            throw new InvalidOperationException(
                    "Only suspended worker memberships can be reactivated."
            );
        }

        workerMembership.setStatus(MembershipStatus.ACTIVE);
        workerMembership.setSuspendedAt(null);
        membershipRepository.save(workerMembership);

        WorkerProfileRules.updateCompletionStatus(
                workerProfile
        );

        WorkerProfile savedWorkerProfile =
                workerProfileRepository.save(
                        workerProfile
                );

        return WorkerProfileMapper.toAdminResponseDTO(
                savedWorkerProfile,
                workerMembership
        );
    }

    /*
     * ============================================================
     * ENTITY LOOKUPS
     * ============================================================
     */

    private WorkerProfile findCurrentWorkerProfileEntity() {

        AppUser currentUser =
                authenticatedUserService.getCurrentUser();

        return workerProfileRepository.findByAppUserId(currentUser.getId()).orElseGet(() -> {
            WorkerProfile profile = new WorkerProfile();
            profile.setAppUser(currentUser);
            return workerProfileRepository.save(profile);
        });
    }

    private WorkerProfile findEntityById(
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

    private WorkerProfile lockWorkerProfileById(
            Long workerProfileId
    ) {
        return workerProfileRepository
                .findByIdForUpdate(workerProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Worker profile not found with ID: "
                                        + workerProfileId
                        )
                );
    }

    /*
     * ============================================================
     * ADMIN PERMISSION
     * ============================================================
     */

    private void validateCurrentUserCanManageWorkers(
            Long companyId
    ) {
        validateSelectedCompany(companyId);

        AppUser currentUser =
                authenticatedUserService.getCurrentUser();

        CompanyMembership membership =
                membershipRepository
                        .findByAppUserIdAndCompanyId(
                                currentUser.getId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedBusinessException(
                                        "You do not have access "
                                                + "to this company."
                                )
                        );

        if (
                membership.getStatus()
                        != MembershipStatus.ACTIVE
        ) {
            throw new AccessDeniedBusinessException(
                    "Your company membership is not active."
            );
        }

        CompanyRole role =
                membership.getRole();

        boolean canManageWorkers =
                role == CompanyRole.OWNER
                        || role == CompanyRole.ADMIN
                        || role == CompanyRole.MANAGER;

        if (!canManageWorkers) {
            throw new AccessDeniedBusinessException(
                    "You do not have permission "
                            + "to manage workers."
            );
        }
    }

    private void validateCurrentUserCanSelfManageProfile() {
        CompanyRole role = companyContext.getRole();
        if (role == null || (role != CompanyRole.WORKER && role != CompanyRole.OWNER && role != CompanyRole.ADMIN && role != CompanyRole.MANAGER && role != CompanyRole.FINANCE)) throw new AccessDeniedBusinessException("Only active company members can access profile self-service.");
    }

    private void validateSelectedCompany(Long companyId) {
        if (!companyId.equals(companyContext.getCompanyId())) {
            throw new AccessDeniedBusinessException(
                    "The selected company does not match the requested company."
            );
        }
    }

    /*
     * ============================================================
     * COMPANY VALIDATION
     * ============================================================
     */

    private CompanyMembership validateWorkerBelongsToCompany(
            WorkerProfile workerProfile,
            Long companyId
    ) {
        CompanyMembership membership =
                membershipRepository
                        .findByAppUserIdAndCompanyId(
                                workerProfile
                                        .getAppUser()
                                        .getId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Worker profile was not found "
                                                + "in this company."
                                )
                        );

        if (
                membership.getRole()
                        != CompanyRole.WORKER
        ) {
            throw new ResourceNotFoundException(
                    "Worker profile was not found "
                            + "in this company."
            );
        }

        return membership;
    }

    private boolean hasAnotherActiveWorkerMembership(
            WorkerProfile workerProfile,
            Long currentCompanyId
    ) {
        return membershipRepository
                .findByAppUserId(
                        workerProfile.getAppUser().getId()
                )
                .stream()
                .anyMatch(membership ->
                        membership.getRole() == CompanyRole.WORKER
                                && membership.getStatus()
                                == MembershipStatus.ACTIVE
                                && !membership.getCompany().getId()
                                .equals(currentCompanyId)
                );
    }
}
