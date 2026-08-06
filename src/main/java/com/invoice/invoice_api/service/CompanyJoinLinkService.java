package com.invoice.invoice_api.service;
import com.invoice.invoice_api.config.InvitationProperties;
import com.invoice.invoice_api.dto.joinLink.*;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.*;
import com.invoice.invoice_api.mapper.CompanyJoinLinkMapper;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.security.SecureTokenService;
import com.invoice.invoice_api.security.JoinLinkTokenCipher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CompanyJoinLinkService {

        private final CompanyJoinLinkRepository joinLinkRepository;
        private final CompanyRepository companyRepository;
        private final CompanyMembershipRepository membershipRepository;
        private final AppUserRepository appUserRepository;
        private final WorkerProfileRepository workerProfileRepository;
        private final SecureTokenService secureTokenService;
        private final InvitationProperties properties;
        private final AuthenticatedUserService authenticatedUserService;
        private final PasswordEncoder passwordEncoder;
        private final CompanyContext companyContext;
        private final JoinLinkTokenCipher tokenCipher;

        @Autowired
        public CompanyJoinLinkService(
                CompanyJoinLinkRepository joinLinkRepository,
                CompanyRepository companyRepository,
                CompanyMembershipRepository membershipRepository,
                AppUserRepository appUserRepository,
                WorkerProfileRepository workerProfileRepository,
                SecureTokenService secureTokenService,
                InvitationProperties properties,
                AuthenticatedUserService authenticatedUserService,
                PasswordEncoder passwordEncoder,
                CompanyContext companyContext,
                JoinLinkTokenCipher tokenCipher
        ) {
            this.joinLinkRepository = joinLinkRepository;
            this.companyRepository = companyRepository;
            this.membershipRepository = membershipRepository;
            this.appUserRepository = appUserRepository;
            this.workerProfileRepository = workerProfileRepository;
            this.secureTokenService = secureTokenService;
            this.properties = properties;
            this.authenticatedUserService = authenticatedUserService;
            this.passwordEncoder = passwordEncoder;
            this.companyContext = companyContext;
            this.tokenCipher = tokenCipher;
        }

        public CompanyJoinLinkService(CompanyJoinLinkRepository joinLinkRepository, CompanyRepository companyRepository, CompanyMembershipRepository membershipRepository, AppUserRepository appUserRepository, WorkerProfileRepository workerProfileRepository, SecureTokenService secureTokenService, InvitationProperties properties, AuthenticatedUserService authenticatedUserService, PasswordEncoder passwordEncoder, CompanyContext companyContext) {
            this(joinLinkRepository, companyRepository, membershipRepository, appUserRepository, workerProfileRepository, secureTokenService, properties, authenticatedUserService, passwordEncoder, companyContext, null);
        }

        /*
         * ============================================================
         * CREATE
         * ============================================================
         */

        @Transactional
        public CompanyJoinLinkCreatedResponseDTO create(
                Long companyId,
                CompanyJoinLinkRequestDTO request
        ) {
            AppUser currentUser =
                    authenticatedUserService.getCurrentUser();

            validateManagementPermission(
                    currentUser,
                    companyId
            );

            Company company = findCompanyById(companyId);
            validateCompanyActive(company);

            validateTargetRole(request.role());
            validateMaxUses(request.maxUses());
            validateExpirationDate(request.expiresAt());

            String rawToken =
                    secureTokenService.generateToken();

            String tokenHash =
                    secureTokenService.hashToken(rawToken);

            CompanyJoinLink joinLink =
                    new CompanyJoinLink();

            joinLink.setCompany(company);
            joinLink.setRole(request.role());
            joinLink.setStatus(JoinLinkStatus.ACTIVE);
            joinLink.setCreatedBy(currentUser);
            joinLink.setTokenHash(tokenHash);
            if (tokenCipher != null) joinLink.setEncryptedToken(tokenCipher.encrypt(rawToken));
            joinLink.setMaxUses(
                    normalizeMaxUses(request.maxUses())
            );
            joinLink.setCurrentUses(0);
            joinLink.setExpiresAt(request.expiresAt());

            CompanyJoinLink savedJoinLink =
                    joinLinkRepository.save(joinLink);

            return new CompanyJoinLinkCreatedResponseDTO(
                    CompanyJoinLinkMapper.toResponseDTO(
                            savedJoinLink
                    ),
                    buildJoinUrl(rawToken)
            );
        }

        /*
         * ============================================================
         * ADMIN QUERIES
         * ============================================================
         */

        @Transactional
        public CompanyJoinLinkResponseDTO findById(
                Long companyId,
                Long joinLinkId
        ) {
            validateCurrentUserCanManage(companyId);

            CompanyJoinLink joinLink =
                    findJoinLinkInCompany(
                            joinLinkId,
                            companyId
                    );
            refreshAvailabilityStatus(joinLink);

            return CompanyJoinLinkMapper.toResponseDTO(joinLink);
        }

        @Transactional(readOnly = true)
        public String findUrl(Long companyId, Long joinLinkId) {
            validateCurrentUserCanManage(companyId);
            CompanyJoinLink joinLink = findJoinLinkInCompany(joinLinkId, companyId);
            if (joinLink.getStatus() != JoinLinkStatus.ACTIVE || joinLink.isExpired() || joinLink.hasReachedUsageLimit()) {
                throw new InvalidOperationException("Only an active join link with remaining quota can be viewed.");
            }
            if (tokenCipher == null || joinLink.getEncryptedToken() == null) {
                throw new InvalidOperationException("This legacy join link cannot be recovered. Create a new link.");
            }
            return buildJoinUrl(tokenCipher.decrypt(joinLink.getEncryptedToken()));
        }

        @Transactional
        public List<CompanyJoinLinkResponseDTO> findByCompany(
                Long companyId
        ) {
            validateCurrentUserCanManage(companyId);
            findCompanyById(companyId);
            refreshCompanyJoinLinks(companyId);

            return joinLinkRepository
                    .findByCompanyIdOrderByCreatedAtDesc(
                            companyId
                    )
                    .stream()
                    .map(CompanyJoinLinkMapper::toResponseDTO)
                    .toList();
        }

        @Transactional
        public List<CompanyJoinLinkResponseDTO> findByCompanyAndStatus(
                Long companyId,
                JoinLinkStatus status
        ) {
            validateCurrentUserCanManage(companyId);
            findCompanyById(companyId);
            refreshCompanyJoinLinks(companyId);

            return joinLinkRepository
                    .findByCompanyIdAndStatusOrderByCreatedAtDesc(
                            companyId,
                            status
                    )
                    .stream()
                    .map(CompanyJoinLinkMapper::toResponseDTO)
                    .toList();
        }

        /*
         * ============================================================
         * PUBLIC QUERY
         * ============================================================
         */

        @Transactional
        public CompanyJoinLinkPublicResponseDTO findPublicByToken(
                String rawToken
        ) {
            CompanyJoinLink joinLink =
                    findByRawToken(rawToken);

            refreshAvailabilityStatus(joinLink);

            return new CompanyJoinLinkPublicResponseDTO(
                    joinLink.getCompany().getName(),
                    joinLink.getRole(),
                    calculateRemainingUses(joinLink),
                    joinLink.getExpiresAt(),
                    joinLink.isAvailable()
            );
        }

        /*
         * ============================================================
         * PUBLIC ACCEPT
         * ============================================================
         */

        @Transactional
        public AcceptCompanyJoinLinkResponseDTO accept(
                AcceptCompanyJoinLinkRequestDTO request
        ) {
            String tokenHash =
                    hashRequiredToken(request.token());

            AppUser appUser =
                    authenticatedUserService.getCurrentUser();
            validateAuthenticatedUser(appUser);

            String normalizedEmail =
                    normalizeEmail(appUser.getEmail());

            CompanyJoinLink snapshot = joinLinkRepository
                    .findByTokenHash(tokenHash)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Join link was not found."
                            )
                    );

            joinLinkRepository.acquireJoinEmailLock(
                    normalizedEmail
            );

            companyRepository
                    .findByIdForUpdate(
                            snapshot.getCompany().getId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Company was not found."
                            )
                    );

            CompanyJoinLink joinLink =
                    joinLinkRepository
                            .findByTokenHashForUpdate(tokenHash)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Join link was not found."
                                    )
                            );

            validateJoinLinkCanBeUsed(joinLink);

            CompanyMembership membership =
                    activateOrCreateMembership(
                            joinLink,
                            appUser
                    );

            createWorkerProfileIfNecessary(appUser);

            joinLink.registerUse();

            if (joinLink.hasReachedUsageLimit()) {
                joinLink.setStatus(
                        JoinLinkStatus.EXPIRED
                );
            }

            CompanyJoinLink savedJoinLink =
                    joinLinkRepository.save(joinLink);

            return new AcceptCompanyJoinLinkResponseDTO(
                    appUser.getId(),
                    appUser.getName(),
                    appUser.getSurname(),
                    appUser.getEmail(),
                    joinLink.getCompany().getId(),
                    joinLink.getCompany().getName(),
                    membership.getId(),
                    membership.getRole(),
                    membership.getStatus(),
                    calculateRemainingUses(savedJoinLink),
                    false,
                    "COMPLETE_WORKER_PROFILE"
            );
        }

        private void validateAuthenticatedUser(AppUser appUser) {
            if (appUser.getStatus() != UserStatus.ACTIVE) {
                throw new InvalidOperationException(
                        "Only active authenticated users can accept public join links."
                );
            }
        }

        /*
         * ============================================================
         * DISABLE
         * ============================================================
         */

        @Transactional
        public CompanyJoinLinkResponseDTO disable(
                Long companyId,
                Long joinLinkId
        ) {
            validateCurrentUserCanManage(companyId);

            CompanyJoinLink joinLink =
                    joinLinkRepository
                            .findByIdAndCompanyIdForUpdate(
                                    joinLinkId,
                                    companyId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Company join link not found with ID: "
                                                    + joinLinkId
                                    )
                            );

            if (
                    joinLink.getStatus()
                            == JoinLinkStatus.DISABLED
            ) {
                throw new InvalidOperationException(
                        "Join link is already disabled."
                );
            }

            if (
                    joinLink.getStatus()
                            == JoinLinkStatus.EXPIRED
            ) {
                throw new InvalidOperationException(
                        "An expired join link cannot be disabled."
                );
            }

            joinLink.setStatus(
                    JoinLinkStatus.DISABLED
            );

            joinLink.setDisabledAt(
                    LocalDateTime.now()
            );

            CompanyJoinLink savedJoinLink =
                    joinLinkRepository.save(joinLink);

            return CompanyJoinLinkMapper.toResponseDTO(
                    savedJoinLink
            );
        }

        @Transactional
        public CompanyJoinLinkResponseDTO activate(Long companyId, Long joinLinkId) {
            validateCurrentUserCanManage(companyId);
            CompanyJoinLink joinLink = joinLinkRepository.findByIdAndCompanyIdForUpdate(joinLinkId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company join link not found with ID: " + joinLinkId));
            if (joinLink.getStatus() != JoinLinkStatus.DISABLED) throw new InvalidOperationException("Only a disabled join link can be reactivated.");
            if (joinLink.isExpired() || joinLink.hasReachedUsageLimit()) throw new InvalidOperationException("This join link can no longer be reactivated because it expired or reached its usage limit.");
            joinLink.setStatus(JoinLinkStatus.ACTIVE);
            joinLink.setDisabledAt(null);
            return CompanyJoinLinkMapper.toResponseDTO(joinLinkRepository.save(joinLink));
        }

        /*
         * ============================================================
         * PERMISSIONS
         * ============================================================
         */

        private void validateCurrentUserCanManage(
                Long companyId
        ) {
            AppUser currentUser =
                    authenticatedUserService.getCurrentUser();

            validateManagementPermission(
                    currentUser,
                    companyId
            );
        }

        private void validateManagementPermission(
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
                        "You do not have permission to manage join links."
                );
            }
        }

        /*
         * ============================================================
         * JOIN LINK VALIDATIONS
         * ============================================================
         */

        private void validateTargetRole(
                CompanyRole role
        ) {
            if (role == null) {
                throw new BusinessException(
                        "Join link role is required."
                );
            }

            if (role != CompanyRole.WORKER) {
                throw new BusinessException(
                        "Public join links can only create WORKER memberships."
                );
            }
        }

        private void validateMaxUses(
                Integer maxUses
        ) {
            if (maxUses == null || maxUses <= 0) {
                throw new BusinessException(
                        "Maximum uses must be greater than zero."
                );
            }
        }

        private void validateExpirationDate(
                LocalDateTime expiresAt
        ) {
            if (expiresAt == null
                    || !expiresAt.isAfter(
                            LocalDateTime.now()
                    )) {
                throw new BusinessException(
                        "Expiration date must be in the future."
                );
            }
        }

        private void validateJoinLinkCanBeUsed(
                CompanyJoinLink joinLink
        ) {
            if (
                    joinLink.getStatus()
                            == JoinLinkStatus.DISABLED
            ) {
                throw new InvalidOperationException(
                        "Join link is disabled."
                );
            }

            if (
                    joinLink.getStatus()
                            == JoinLinkStatus.EXPIRED
            ) {
                throw new InvalidOperationException(
                        "Join link has expired."
                );
            }

            if (
                    joinLink.isExpired()
                            || joinLink.hasReachedUsageLimit()
            ) {
                joinLink.setStatus(
                        JoinLinkStatus.EXPIRED
                );

                joinLinkRepository.save(joinLink);

                throw new InvalidOperationException(
                        "Join link has expired."
                );
            }

            if (
                    joinLink.getStatus()
                            != JoinLinkStatus.ACTIVE
            ) {
                throw new InvalidOperationException(
                        "Join link is not available."
                );
            }

            validateCompanyActive(
                    joinLink.getCompany()
            );
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

        /*
         * ============================================================
         * USER VALIDATIONS
         * ============================================================
         */

        /*
         * ============================================================
         * MEMBERSHIP
         * ============================================================
         */

        private CompanyMembership activateOrCreateMembership(
                CompanyJoinLink joinLink,
                AppUser appUser
        ) {
            Optional<CompanyMembership> existingMembership =
                    membershipRepository
                            .findByAppUserIdAndCompanyId(
                                    appUser.getId(),
                                    joinLink.getCompany().getId()
                            );

            LocalDateTime now =
                    LocalDateTime.now();

            if (existingMembership.isPresent()) {

                CompanyMembership membership =
                        existingMembership.get();

                if (membership.getRole() != CompanyRole.WORKER) {
                    throw new InvalidOperationException(
                            "A non-worker membership cannot be replaced through a public join link."
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
                        joinLink.getRole()
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
                    joinLink.getCompany()
            );
            membership.setRole(
                    joinLink.getRole()
            );
            membership.setStatus(
                    MembershipStatus.ACTIVE
            );
            membership.setAcceptedAt(now);

            return membershipRepository.save(
                    membership
            );
        }

        /*
         * ============================================================
         * WORKER PROFILE
         * ============================================================
         */

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
                    WorkerProfileRules.updateCompletionStatus(workerProfile);
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

        private CompanyJoinLink findJoinLinkInCompany(
                Long joinLinkId,
                Long companyId
        ) {
            CompanyJoinLink joinLink =
                    joinLinkRepository
                            .findByIdAndCompanyId(joinLinkId, companyId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Company join link not found with ID: "
                                                    + joinLinkId
                                    )
                            );

            return joinLink;
        }

        private CompanyJoinLink findByRawToken(
                String rawToken
        ) {
            String tokenHash =
                    hashRequiredToken(rawToken);

            return joinLinkRepository
                    .findByTokenHash(tokenHash)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Join link was not found."
                            )
                    );
        }

        /*
         * ============================================================
         * TOKEN / STATUS
         * ============================================================
         */

        private String hashRequiredToken(
                String rawToken
        ) {
            if (
                    rawToken == null
                            || rawToken.isBlank()
            ) {
                throw new BusinessException(
                        "Join link token is required."
                );
            }

            return secureTokenService.hashToken(
                    rawToken.trim()
            );
        }

        private void refreshAvailabilityStatus(
                CompanyJoinLink joinLink
        ) {
            if (
                    joinLink.getStatus()
                            != JoinLinkStatus.ACTIVE
            ) {
                return;
            }

            if (
                    joinLink.isExpired()
                            || joinLink.hasReachedUsageLimit()
            ) {
                joinLink.setStatus(
                        JoinLinkStatus.EXPIRED
                );

                joinLinkRepository.save(joinLink);
            }
        }

        private void refreshCompanyJoinLinks(Long companyId) {
            joinLinkRepository
                    .findByCompanyIdAndStatusOrderByCreatedAtDesc(
                            companyId,
                            JoinLinkStatus.ACTIVE
                    )
                    .forEach(this::refreshAvailabilityStatus);
        }

        /*
         * ============================================================
         * NORMALIZATION / URL
         * ============================================================
         */

        private Integer normalizeMaxUses(
                Integer maxUses
        ) {
            return maxUses;
        }

        private int calculateRemainingUses(
                CompanyJoinLink joinLink
        ) {
            return Math.max(
                    joinLink.getMaxUses()
                            - joinLink.getCurrentUses(),
                    0
            );
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

        private String buildJoinUrl(
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
                    + "/join?token="
                    + rawToken;
        }
}
