package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRequestDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRoleRequestDTO;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.WorkerProfileStatus;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.CompanyMembershipMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyMembershipService {

    private final CompanyMembershipRepository membershipRepository;
    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final CompanyContext companyContext;

    public CompanyMembershipService(
            CompanyMembershipRepository membershipRepository,
            AppUserRepository appUserRepository,
            CompanyRepository companyRepository,
            WorkerProfileRepository workerProfileRepository,
            CompanyContext companyContext
    ) {
        this.membershipRepository = membershipRepository;
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.companyContext = companyContext;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public CompanyMembershipResponseDTO create(
            CompanyMembershipRequestDTO request
    ) {
        throw new InvalidOperationException(
                "Memberships must be created through an invitation or public join link"
        );
        /*
        AppUser appUser = findAppUserById(request.appUserId());
        Company company = findCompanyById(request.companyId());

        CompanyMembership existingMembership = membershipRepository
                .findByAppUserIdAndCompanyId(
                        request.appUserId(),
                        request.companyId()
                )
                .orElse(null);

        if (existingMembership != null) {
            return reactivateExistingMembership(
                    existingMembership,
                    request
            );
        }

        CompanyMembership membership = new CompanyMembership();

        membership.setAppUser(appUser);
        membership.setCompany(company);
        membership.setRole(request.role());
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setAcceptedAt(LocalDateTime.now());

        CompanyMembership savedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(savedMembership);
        */
    }

    // =========================================================
    // READ
    // =========================================================

    @Transactional(readOnly = true)
    public CompanyMembershipResponseDTO findById(Long id) {
        Long companyId = companyContext.getCompanyId();
        requireMembershipReader(companyId);
        CompanyMembership membership = findMembershipByIdAndCompany(
                id,
                companyId
        );

        return CompanyMembershipMapper.toResponseDTO(membership);
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findByUserId(
            Long appUserId
    ) {
        Long companyId = companyContext.getCompanyId();
        requireMembershipReader(companyId);

        return membershipRepository
                .findByAppUserId(appUserId)
                .stream()
                .filter(membership -> membership.getCompany().getId()
                        .equals(companyId))
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findByCompanyId(
            Long companyId
    ) {
        requireMembershipReader(companyId);
        findCompanyById(companyId);

        return membershipRepository
                .findByCompanyId(companyId)
                .stream()
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findActiveByCompanyId(
            Long companyId
    ) {
        requireMembershipReader(companyId);
        findCompanyById(companyId);

        return membershipRepository
                .findByCompanyIdAndStatus(
                        companyId,
                        MembershipStatus.ACTIVE
                )
                .stream()
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    // =========================================================
    // UPDATE ROLE
    // =========================================================

    @Transactional
    public CompanyMembershipResponseDTO updateRole(
            Long membershipId,
            CompanyMembershipRoleRequestDTO request
    ) {
        return updateRole(
                companyContext.getCompanyId(),
                membershipId,
                request
        );
    }

    @Transactional
    public CompanyMembershipResponseDTO updateRole(
            Long companyId,
            Long membershipId,
            CompanyMembershipRoleRequestDTO request
    ) {
        requireRoleManager(companyId);

        CompanyMembership membershipSnapshot =
                findMembershipByIdAndCompany(
                        membershipId,
                        companyId
                );

        workerProfileRepository
                .findByAppUserId(
                        membershipSnapshot.getAppUser().getId()
                )
                .ifPresent(profile ->
                        workerProfileRepository.findByIdForUpdate(
                                profile.getId()
                        )
                );

        CompanyMembership membership =
                lockMembershipByIdAndCompany(
                        membershipId,
                        companyId
                );

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Only active memberships can change role"
            );
        }

        CompanyRole currentRole = membership.getRole();
        CompanyRole requestedRole = request.role();

        if ((currentRole != CompanyRole.WORKER
                && currentRole != CompanyRole.MANAGER
                && currentRole != CompanyRole.FINANCE)
                || (requestedRole != CompanyRole.MANAGER
                && requestedRole != CompanyRole.FINANCE)) {
            throw new InvalidOperationException(
                    "This endpoint can only promote workers or alternate MANAGER and FINANCE roles"
            );
        }

        membership.setRole(requestedRole);

        if (currentRole == CompanyRole.WORKER) {
            suspendProfileWithoutWorkerMemberships(membership);
        }

        CompanyMembership updatedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(updatedMembership);
    }

    // =========================================================
    // STATUS
    // =========================================================

    @Transactional
    public void deactivate(Long id) {
        Long companyId = companyContext.getCompanyId();
        requireRoleManager(companyId);
        CompanyMembership membership = lockMembershipByIdAndCompany(id, companyId);
        validateStatusTargetRole(membership);

        if (membership.getStatus() == MembershipStatus.SUSPENDED) {
            return;
        }

        membership.setStatus(MembershipStatus.SUSPENDED);
        membership.setSuspendedAt(LocalDateTime.now());

        membershipRepository.save(membership);
    }

    @Transactional
    public CompanyMembershipResponseDTO reactivate(Long id) {
        Long companyId = companyContext.getCompanyId();
        requireRoleManager(companyId);
        CompanyMembership membership = lockMembershipByIdAndCompany(id, companyId);
        validateStatusTargetRole(membership);

        if (membership.getStatus() == MembershipStatus.ACTIVE) {
            return CompanyMembershipMapper.toResponseDTO(membership);
        }

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setSuspendedAt(null);

        CompanyMembership reactivatedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(
                reactivatedMembership
        );
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private CompanyMembershipResponseDTO reactivateExistingMembership(
            CompanyMembership existingMembership,
            CompanyMembershipRequestDTO request
    ) {
        if (existingMembership.getStatus() == MembershipStatus.ACTIVE) {
            throw new DuplicateResourceException(
                    "User is already a member of this company"
            );
        }

        existingMembership.setRole(request.role());
        existingMembership.setStatus(MembershipStatus.ACTIVE);
        existingMembership.setAcceptedAt(LocalDateTime.now());

        existingMembership.setSuspendedAt(null);
        existingMembership.setRejectedAt(null);

        CompanyMembership reactivatedMembership =
                membershipRepository.save(existingMembership);

        return CompanyMembershipMapper.toResponseDTO(
                reactivatedMembership
        );
    }

    private CompanyMembership findMembershipById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company membership not found with ID: " + id
                        )
                );
    }

    private CompanyMembership findMembershipByIdAndCompany(
            Long membershipId,
            Long companyId
    ) {
        return membershipRepository
                .findById(membershipId)
                .filter(membership -> membership.getCompany().getId()
                        .equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company membership not found with ID: "
                                + membershipId
                ));
    }

    private CompanyMembership lockMembershipByIdAndCompany(
            Long membershipId,
            Long companyId
    ) {
        return membershipRepository
                .findByIdAndCompanyIdForUpdate(membershipId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company membership not found with ID: "
                                + membershipId
                ));
    }

    private void requireMembershipReader(Long companyId) {
        requireSelectedCompany(companyId);
        CompanyRole role = companyContext.getRole();
        if (role != CompanyRole.OWNER
                && role != CompanyRole.ADMIN
                && role != CompanyRole.MANAGER) {
            throw new AccessDeniedBusinessException(
                    "You do not have permission to view company memberships"
            );
        }
    }

    private void requireRoleManager(Long companyId) {
        requireSelectedCompany(companyId);
        CompanyRole role = companyContext.getRole();
        if (role != CompanyRole.OWNER
                && role != CompanyRole.ADMIN) {
            throw new AccessDeniedBusinessException(
                    "Only owners and administrators can manage membership roles"
            );
        }
    }

    private void requireSelectedCompany(Long companyId) {
        if (!companyId.equals(companyContext.getCompanyId())) {
            throw new AccessDeniedBusinessException(
                    "The selected company does not match the requested company"
            );
        }
    }

    private void validateStatusTargetRole(
            CompanyMembership membership
    ) {
        if (membership.getRole() != CompanyRole.MANAGER
                && membership.getRole() != CompanyRole.FINANCE) {
            throw new InvalidOperationException(
                    "This endpoint only changes MANAGER or FINANCE membership status"
            );
        }
    }

    private void suspendProfileWithoutWorkerMemberships(
            CompanyMembership promotedMembership
    ) {
        Long appUserId = promotedMembership.getAppUser().getId();
        boolean hasActiveWorkerMembership = membershipRepository
                .findByAppUserId(appUserId)
                .stream()
                .anyMatch(membership ->
                        membership.getRole() == CompanyRole.WORKER
                                && membership.getStatus()
                                == MembershipStatus.ACTIVE
                );

        if (hasActiveWorkerMembership) {
            return;
        }

        workerProfileRepository.findByAppUserId(appUserId)
                .ifPresent(profile -> {
                    profile.setStatus(WorkerProfileStatus.SUSPENDED);
                    workerProfileRepository.save(profile);
                });
    }

    private AppUser findAppUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "App user not found with ID: " + id
                        )
                );
    }

    private Company findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with ID: " + id
                        )
                );
    }
}
