package com.invoice.invoice_api.service;


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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final WorkerProfileValidator workerProfileValidator;

    public WorkerProfileService(
            WorkerProfileRepository workerProfileRepository,
            CompanyMembershipRepository membershipRepository,
            AuthenticatedUserService authenticatedUserService,
            WorkerProfileValidator workerProfileValidator
    ) {
        this.workerProfileRepository = workerProfileRepository;
        this.membershipRepository = membershipRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.workerProfileValidator = workerProfileValidator;
    }

    /*
     * ============================================================
     * CURRENT USER
     * ============================================================
     */

    @Transactional(readOnly = true)
    public WorkerProfileResponseDTO findCurrentProfile() {

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
    public WorkerProfileResponseDTO findById(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                findEntityById(workerProfileId);

        validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        return WorkerProfileMapper.toResponseDTO(
                workerProfile
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerProfileSummaryDTO> findActiveWorkersByCompany(
            Long companyId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        return workerProfileRepository
                .findActiveWorkersByCompanyId(companyId)
                .stream()
                .map(WorkerProfileMapper::toSummaryDTO)
                .toList();
    }

    /*
     * ============================================================
     * SUSPEND
     * ============================================================
     */

    @Transactional
    public WorkerProfileResponseDTO suspend(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                findEntityById(workerProfileId);

        validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        if (
                workerProfile.getStatus()
                        == WorkerProfileStatus.SUSPENDED
        ) {
            throw new InvalidOperationException(
                    "Worker profile is already suspended."
            );
        }

        workerProfile.setStatus(
                WorkerProfileStatus.SUSPENDED
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
     * REACTIVATE
     * ============================================================
     */

    @Transactional
    public WorkerProfileResponseDTO reactivate(
            Long companyId,
            Long workerProfileId
    ) {
        validateCurrentUserCanManageWorkers(
                companyId
        );

        WorkerProfile workerProfile =
                findEntityById(workerProfileId);

        validateWorkerBelongsToCompany(
                workerProfile,
                companyId
        );

        if (
                workerProfile.getStatus()
                        != WorkerProfileStatus.SUSPENDED
        ) {
            throw new InvalidOperationException(
                    "Only suspended worker profiles can be reactivated."
            );
        }

        WorkerProfileRules.updateCompletionStatus(
                workerProfile
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
     * ENTITY LOOKUPS
     * ============================================================
     */

    private WorkerProfile findCurrentWorkerProfileEntity() {

        AppUser currentUser =
                authenticatedUserService.getCurrentUser();

        return workerProfileRepository
                .findByAppUserId(
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Worker profile was not found "
                                        + "for the current user."
                        )
                );
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

    /*
     * ============================================================
     * ADMIN PERMISSION
     * ============================================================
     */

    private void validateCurrentUserCanManageWorkers(
            Long companyId
    ) {
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

    /*
     * ============================================================
     * COMPANY VALIDATION
     * ============================================================
     */

    private void validateWorkerBelongsToCompany(
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
    }
}
