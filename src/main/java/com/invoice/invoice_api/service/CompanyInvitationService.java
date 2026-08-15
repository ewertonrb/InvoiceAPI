package com.invoice.invoice_api.service;

import com.invoice.invoice_api.config.InvitationProperties;
import com.invoice.invoice_api.dto.companyInvitation.*;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.*;
import com.invoice.invoice_api.mapper.CompanyInvitationMapper;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.security.SecureTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CompanyInvitationService {

    private final CompanyInvitationRepository invitationRepository;
    private final CompanyRepository companyRepository;
    private final AppUserRepository appUserRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final SecureTokenService secureTokenService;
    private final InvitationProperties properties;
    private final AuthenticatedUserService authenticatedUserService;
    private final WorkerProfileRepository workerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyContext companyContext;
    private final NotificationEmailService notificationEmailService;

    public CompanyInvitationService(
            CompanyInvitationRepository invitationRepository,
            CompanyRepository companyRepository,
            AppUserRepository appUserRepository,
            WorkerProfileRepository workerProfileRepository,
            PasswordEncoder passwordEncoder,
            CompanyMembershipRepository membershipRepository,
            SecureTokenService secureTokenService,
            InvitationProperties properties,
            AuthenticatedUserService authenticatedUserService,
            CompanyContext companyContext,
            NotificationEmailService notificationEmailService
    ) {
        this.invitationRepository = invitationRepository;
        this.companyRepository = companyRepository;
        this.appUserRepository = appUserRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.membershipRepository = membershipRepository;
        this.secureTokenService = secureTokenService;
        this.properties = properties;
        this.authenticatedUserService = authenticatedUserService;
        this.companyContext = companyContext;
        this.notificationEmailService = notificationEmailService;
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */

    @Transactional
    public CompanyInvitationCreatedResponseDTO createInvitation(
            Long companyId,
            CompanyInvitationRequestDTO request
    ) {
        AppUser currentUser =
                authenticatedUserService.getCurrentUser();

        validateInvitationPermission(
                currentUser,
                companyId
        );

        String normalizedEmail =
                normalizeEmail(request.email());

        lockInvitationEmail(normalizedEmail);
        Company company = lockCompanyById(companyId);
        validateCompanyActive(company);

        validateTargetRole(request.role());

        validateUserIsNotAlreadyActiveMember(
                companyId,
                normalizedEmail
        );

        expirePreviousPendingInvitationIfNecessary(
                companyId,
                normalizedEmail
        );

        String rawToken =
                secureTokenService.generateToken();

        String tokenHash =
                secureTokenService.hashToken(rawToken);

        CompanyInvitation invitation =
                new CompanyInvitation();

        invitation.setCompany(company);
        invitation.setName(
                normalizeRequiredText(
                        request.name(),
                        "Name is required."
                )
        );
        invitation.setSurname(
                normalizeRequiredText(
                        request.surname(),
                        "Surname is required."
                )
        );
        invitation.setEmail(normalizedEmail);
        invitation.setRole(request.role());
        invitation.setInvitedBy(currentUser);
        invitation.setTokenHash(tokenHash);
        invitation.setStatus(
                InvitationStatus.PENDING
        );
        invitation.setExpiresAt(
                LocalDateTime.now().plusDays(
                        properties
                                .getInvitation()
                                .getExpirationDays()
                )
        );

        CompanyInvitation savedInvitation =
                invitationRepository.save(invitation);

        String invitationUrl =
                buildInvitationUrl(rawToken);

        notificationEmailService.sendInvitationEmail(
                savedInvitation.getEmail(),
                savedInvitation.getName(),
                company.getName(),
                invitationUrl,
                properties.getInvitation().getExpirationDays()
        );

        return new CompanyInvitationCreatedResponseDTO(
                CompanyInvitationMapper.toResponseDTO(
                        savedInvitation
                ),
                invitationUrl
        );
    }

    /*
     * ============================================================
     * OWNER QUERIES
     * ============================================================
     */

    @Transactional
    public CompanyInvitationResponseDTO findById(
            Long companyId,
            Long invitationId
    ) {
        validateCurrentUserCanManageInvitations(
                companyId
        );

        CompanyInvitation invitation =
                findInvitationInCompany(
                        invitationId,
                        companyId
                );
        refreshExpiredStatus(invitation);

        return CompanyInvitationMapper.toResponseDTO(
                invitation
        );
    }

    @Transactional
    public List<CompanyInvitationResponseDTO> findByCompany(
            Long companyId
    ) {
        validateCurrentUserCanManageInvitations(
                companyId
        );

        findCompanyById(companyId);
        expirePendingInvitations(companyId);

        return invitationRepository
                .findByCompanyIdOrderByCreatedAtDesc(
                        companyId
                )
                .stream()
                .map(
                        CompanyInvitationMapper::toResponseDTO
                )
                .toList();
    }

    @Transactional
    public List<CompanyInvitationResponseDTO> findByCompanyAndStatus(
            Long companyId,
            InvitationStatus status
    ) {
        validateCurrentUserCanManageInvitations(
                companyId
        );

        findCompanyById(companyId);
        expirePendingInvitations(companyId);

        return invitationRepository
                .findByCompanyIdAndStatusOrderByCreatedAtDesc(
                        companyId,
                        status
                )
                .stream()
                .map(
                        CompanyInvitationMapper::toResponseDTO
                )
                .toList();
    }

    /*
     * ============================================================
     * PUBLIC TOKEN VALIDATION
     * ============================================================
     */

    @Transactional
    public CompanyInvitationPublicResponseDTO findPublicInvitation(
            String rawToken
    ) {
        CompanyInvitation invitation =
                findInvitationByRawToken(rawToken);

        refreshExpiredStatus(invitation);

        boolean valid =
                invitation.getStatus()
                        == InvitationStatus.PENDING
                        && !invitation.isExpired();

        return new CompanyInvitationPublicResponseDTO(
                invitation.getCompany().getName(),
                invitation.getName(),
                invitation.getSurname(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getExpiresAt(),
                valid
        );
    }

    /*
     * ============================================================
     * CANCEL
     * ============================================================
     */

    @Transactional
    public CompanyInvitationResponseDTO cancel(
            Long companyId,
            Long invitationId
    ) {
        validateCurrentUserCanManageInvitations(
                companyId
        );

        CompanyInvitation invitation =
                findInvitationInCompany(
                        invitationId,
                        companyId
                );

        if (
                invitation.getStatus()
                        == InvitationStatus.ACCEPTED
        ) {
            throw new BusinessException(
                    "An accepted invitation cannot be cancelled."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.CANCELLED
        ) {
            throw new BusinessException(
                    "Invitation is already cancelled."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.EXPIRED
        ) {
            throw new BusinessException(
                    "An expired invitation cannot be cancelled."
            );
        }

        invitation.setStatus(
                InvitationStatus.CANCELLED
        );

        invitation.setCancelledAt(
                LocalDateTime.now()
        );

        CompanyInvitation cancelledInvitation =
                invitationRepository.save(invitation);

        return CompanyInvitationMapper.toResponseDTO(
                cancelledInvitation
        );
    }

    @Transactional
    public void decline(
            DeclineCompanyInvitationRequestDTO request
    ) {
        CompanyInvitation invitationSnapshot =
                findInvitationByRawToken(request.token());

        lockInvitationEmail(invitationSnapshot.getEmail());
        lockCompanyById(
                invitationSnapshot.getCompany().getId()
        );

        CompanyInvitation invitation =
                findInvitationByRawTokenForUpdate(request.token());

        refreshExpiredStatus(invitation);

        if (
                invitation.getStatus()
                        == InvitationStatus.EXPIRED
        ) {
            throw new InvalidOperationException(
                    "Invitation has expired."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.ACCEPTED
        ) {
            throw new InvalidOperationException(
                    "An accepted invitation cannot be declined."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.CANCELLED
        ) {
            throw new InvalidOperationException(
                    "A cancelled invitation cannot be declined."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.DECLINED
        ) {
            return;
        }

        invitation.setStatus(
                InvitationStatus.DECLINED
        );

        invitation.setDeclinedAt(
                LocalDateTime.now()
        );

        invitationRepository.save(invitation);
    }

    /*
     * ============================================================
     * PERMISSIONS
     * ============================================================
     */

    @Transactional
    public AcceptCompanyInvitationResponseDTO accept(
            AcceptCompanyInvitationRequestDTO request
    ) {
        validatePasswordConfirmation(request);

        CompanyInvitation invitationSnapshot =
                findInvitationByRawToken(request.token());

        lockInvitationEmail(invitationSnapshot.getEmail());
        lockCompanyById(
                invitationSnapshot.getCompany().getId()
        );

        CompanyInvitation invitation =
                findInvitationByRawTokenForUpdate(
                        request.token()
                );

        validateInvitationCanBeAccepted(invitation);

        Optional<AppUser> existingUser =
                appUserRepository.findByEmailIgnoreCase(
                        invitation.getEmail()
                );

        AppUser appUser;
        boolean newAccountCreated;

        if (existingUser.isPresent()) {
            appUser = existingUser.get();

            validateExistingUserCanAccept(
                    appUser,
                    request.password()
            );

            newAccountCreated = false;

        } else {
            appUser = createAppUserFromInvitation(
                    invitation,
                    request.password()
            );

            newAccountCreated = true;
        }

        createWorkerProfileIfNecessary(appUser);

        CompanyMembership membership =
                activateOrCreateMembership(
                        invitation,
                        appUser
                );

        invitation.setStatus(
                InvitationStatus.ACCEPTED
        );

        invitation.setAcceptedAt(
                LocalDateTime.now()
        );

        invitation.setCancelledAt(null);
        invitation.setDeclinedAt(null);

        invitationRepository.save(invitation);

        return new AcceptCompanyInvitationResponseDTO(
                appUser.getId(),
                appUser.getName(),
                appUser.getSurname(),
                appUser.getEmail(),
                invitation.getCompany().getId(),
                invitation.getCompany().getName(),
                membership.getId(),
                membership.getRole(),
                membership.getStatus(),
                newAccountCreated
        );
    }

    private CompanyMembership activateOrCreateMembership(
            CompanyInvitation invitation,
            AppUser appUser
    ) {
        Optional<CompanyMembership> existingMembership =
                membershipRepository
                        .findByAppUserIdAndCompanyId(
                                appUser.getId(),
                                invitation.getCompany().getId()
                        );

        LocalDateTime now =
                LocalDateTime.now();

        if (existingMembership.isPresent()) {
            CompanyMembership membership =
                    existingMembership.get();

            if (membership.getRole() != CompanyRole.WORKER) {
                throw new InvalidOperationException(
                        "An existing non-worker membership cannot be replaced by a worker invitation."
                );
            }

            if (
                    membership.getStatus()
                            == MembershipStatus.ACTIVE
            ) {
                throw new DuplicateResourceException(
                        "User is already an active member of this company."
                );
            }

            membership.setRole(
                    invitation.getRole()
            );

            membership.setStatus(
                    MembershipStatus.ACTIVE
            );

            membership.setAcceptedAt(now);
            membership.setRejectedAt(null);
            membership.setSuspendedAt(null);

            return membershipRepository.save(
                    membership
            );
        }

        CompanyMembership membership =
                new CompanyMembership();

        membership.setAppUser(appUser);
        membership.setCompany(
                invitation.getCompany()
        );

        membership.setRole(
                invitation.getRole()
        );

        membership.setStatus(
                MembershipStatus.ACTIVE
        );

        membership.setAcceptedAt(now);

        return membershipRepository.save(
                membership
        );
    }

    private void createWorkerProfileIfNecessary(
            AppUser appUser
    ) {
        Optional<WorkerProfile> existingProfile =
                workerProfileRepository.findByAppUserId(
                        appUser.getId()
                );

        if (existingProfile.isPresent()) {
            WorkerProfile workerProfile = existingProfile.get();

            if (workerProfile.getStatus()
                    == WorkerProfileStatus.SUSPENDED) {
                WorkerProfileRules.updateCompletionStatus(
                        workerProfile
                );
                workerProfileRepository.save(workerProfile);
            }

            return;
        }

        WorkerProfile workerProfile =
                new WorkerProfile();

        workerProfile.setAppUser(appUser);
        workerProfile.setStatus(
                WorkerProfileStatus.INCOMPLETE
        );

        workerProfile.setGstRegistered(false);

        workerProfileRepository.save(
                workerProfile
        );
    }

    private void validateCurrentUserCanManageInvitations(
            Long companyId
    ) {
        AppUser currentUser =
                authenticatedUserService.getCurrentUser();

        validateInvitationPermission(
                currentUser,
                companyId
        );
    }

    private void validateInvitationPermission(
            AppUser currentUser,
            Long companyId
    ) {
        if (!companyId.equals(companyContext.getCompanyId())) {
            throw new AccessDeniedBusinessException(
                    "The selected company does not match the requested company."
            );
        }

        CompanyMembership membership =
                membershipRepository
                        .findByAppUserIdAndCompanyId(
                                currentUser.getId(),
                                companyId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedBusinessException(
                                        "You do not have access to this company."
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

        boolean allowed =
                role == CompanyRole.OWNER
                        || role == CompanyRole.ADMIN
                        || role == CompanyRole.MANAGER;

        if (!allowed) {
            throw new AccessDeniedBusinessException(
                    "You do not have permission to manage invitations."
            );
        }
    }

    /*
     * ============================================================
     * TARGET VALIDATIONS
     * ============================================================
     */

    private void validateTargetRole(
            CompanyRole targetRole
    ) {
        if (targetRole == null) {
            throw new BusinessException(
                    "Invitation role is required."
            );
        }

        /*
         * Este endpoint será usado para adicionar workers.
         * Assim ninguém consegue criar outro OWNER por convite.
         */
        if (targetRole != CompanyRole.WORKER) {
            throw new BusinessException(
                    "This invitation endpoint only accepts the WORKER role."
            );
        }
    }
    private void validatePasswordConfirmation(
            AcceptCompanyInvitationRequestDTO request
    ) {
        if (!request.password().equals(
                request.confirmPassword()
        )) {
            throw new BusinessException(
                    "Password and password confirmation do not match."
            );
        }
    }

    private void validateInvitationCanBeAccepted(
            CompanyInvitation invitation
    ) {
        refreshExpiredStatus(invitation);

        if (
                invitation.getStatus()
                        == InvitationStatus.EXPIRED
        ) {
            throw new InvalidOperationException(
                    "Invitation has expired."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.CANCELLED
        ) {
            throw new InvalidOperationException(
                    "Invitation was cancelled."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.DECLINED
        ) {
            throw new InvalidOperationException(
                    "Invitation was declined."
            );
        }

        if (
                invitation.getStatus()
                        == InvitationStatus.ACCEPTED
        ) {
            throw new InvalidOperationException(
                    "Invitation has already been accepted."
            );
        }

        if (
                invitation.getStatus()
                        != InvitationStatus.PENDING
        ) {
            throw new InvalidOperationException(
                    "Invitation cannot be accepted."
            );
        }

        validateCompanyActive(
                invitation.getCompany()
        );
    }

    private void validateExistingUserCanAccept(
            AppUser appUser,
            String password
    ) {
        if (
                appUser.getStatus()
                        == UserStatus.BLOCKED
        ) {
            throw new InvalidOperationException(
                    "User account is blocked."
            );
        }

        if (
                appUser.getStatus()
                        == UserStatus.DELETED
        ) {
            throw new InvalidOperationException(
                    "User account was deleted."
            );
        }

        if (
                appUser.getStatus()
                        != UserStatus.ACTIVE
        ) {
            throw new InvalidOperationException(
                    "User account is not active."
            );
        }

        if (
                !passwordEncoder.matches(
                        password,
                        appUser.getPassword()
                )
        ) {
            throw new AccessDeniedBusinessException(
                    "Invalid email or password."
            );
        }
    }

    private AppUser createAppUserFromInvitation(
            CompanyInvitation invitation,
            String rawPassword
    ) {
        AppUser appUser = new AppUser();

        appUser.setName(
                invitation.getName()
        );

        appUser.setSurname(
                invitation.getSurname()
        );

        appUser.setEmail(
                normalizeEmail(
                        invitation.getEmail()
                )
        );

        appUser.setPassword(
                passwordEncoder.encode(
                        rawPassword
                )
        );

        appUser.setStatus(
                UserStatus.ACTIVE
        );

        return appUserRepository.save(appUser);
    }

    private void validateUserIsNotAlreadyActiveMember(
            Long companyId,
            String email
    ) {
        Optional<AppUser> existingUser =
                appUserRepository
                        .findByEmailIgnoreCase(email);

        if (existingUser.isEmpty()) {
            return;
        }

        membershipRepository
                .findByAppUserIdAndCompanyId(
                        existingUser.get().getId(),
                        companyId
                )
                .filter(membership ->
                        membership.getStatus()
                                == MembershipStatus.ACTIVE
                )
                .ifPresent(membership -> {
                    throw new DuplicateResourceException(
                            "User is already an active member of this company."
                    );
                });
    }

    private void expirePreviousPendingInvitationIfNecessary(
            Long companyId,
            String email
    ) {
        Optional<CompanyInvitation> pendingInvitation =
                invitationRepository
                        .findByCompanyIdAndEmailIgnoreCaseAndStatus(
                                companyId,
                                email,
                                InvitationStatus.PENDING
                        );

        if (pendingInvitation.isEmpty()) {
            return;
        }

        CompanyInvitation existingInvitation =
                pendingInvitation.get();

        if (!existingInvitation.isExpired()) {
            throw new DuplicateResourceException(
                    "There is already a pending invitation for this email."
            );
        }

        existingInvitation.setStatus(
                InvitationStatus.EXPIRED
        );

        invitationRepository.save(
                existingInvitation
        );
    }

    /*
     * ============================================================
     * LOOKUPS
     * ============================================================
     */

    private Company findCompanyById(
            Long companyId
    ) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with ID: "
                                        + companyId
                        )
                );
    }

    private Company lockCompanyById(Long companyId) {
        return companyRepository
                .findByIdForUpdate(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with ID: "
                                        + companyId
                        )
                );
    }

    private void lockInvitationEmail(String email) {
        invitationRepository.acquireInvitationEmailLock(email);
    }

    private CompanyInvitation findInvitationInCompany(
            Long invitationId,
            Long companyId
    ) {
        CompanyInvitation invitation =
                invitationRepository
                        .findByIdAndCompanyId(
                                invitationId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Company invitation not found with ID: "
                                                + invitationId
                                )
                        );

        return invitation;
    }

    private CompanyInvitation findInvitationByRawToken(
            String rawToken
    ) {
        if (
                rawToken == null
                        || rawToken.isBlank()
        ) {
            throw new BusinessException(
                    "Invitation token is required."
            );
        }

        String tokenHash =
                secureTokenService.hashToken(
                        rawToken.trim()
                );

        return invitationRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invitation was not found."
                        )
                );
    }

    private CompanyInvitation findInvitationByRawTokenForUpdate(
            String rawToken
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(
                    "Invitation token is required."
            );
        }

        String tokenHash = secureTokenService.hashToken(
                rawToken.trim()
        );

        return invitationRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invitation was not found."
                        )
                );
    }

    /*
     * ============================================================
     * STATUS / URL / NORMALIZATION
     * ============================================================
     */

    private void refreshExpiredStatus(
            CompanyInvitation invitation
    ) {
        if (
                invitation.getStatus()
                        == InvitationStatus.PENDING
                        && invitation.isExpired()
        ) {
            invitation.setStatus(
                    InvitationStatus.EXPIRED
            );

            invitationRepository.save(invitation);
        }
    }

    private void expirePendingInvitations(Long companyId) {
        invitationRepository
                .findByCompanyIdAndStatusOrderByCreatedAtDesc(
                        companyId,
                        InvitationStatus.PENDING
                )
                .forEach(this::refreshExpiredStatus);
    }

    private void validateCompanyActive(
            Company company
    ) {
        if (
                !Boolean.TRUE.equals(
                        company.getActive()
                )
        ) {
            throw new BusinessException(
                    "Company is inactive."
            );
        }
    }

    private String buildInvitationUrl(
            String rawToken
    ) {
        String baseUrl =
                properties
                        .getFrontend()
                        .getBaseUrl();

        if (
                baseUrl == null
                        || baseUrl.isBlank()
        ) {
            throw new IllegalStateException(
                    "Frontend base URL is not configured."
            );
        }

        String normalizedBaseUrl =
                baseUrl.endsWith("/")
                        ? baseUrl.substring(
                        0,
                        baseUrl.length() - 1
                )
                        : baseUrl;

        return normalizedBaseUrl
                + "/invitations/accept?token="
                + rawToken;
    }

    private String normalizeEmail(
            String email
    ) {
        if (
                email == null
                        || email.isBlank()
        ) {
            throw new BusinessException(
                    "Email is required."
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
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
}
