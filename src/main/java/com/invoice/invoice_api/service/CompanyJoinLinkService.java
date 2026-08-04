package com.invoice.invoice_api.service;
import com.invoice.invoice_api.config.InvitationProperties;
import com.invoice.invoice_api.dto.joinLink.*;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.*;
import com.invoice.invoice_api.mapper.CompanyJoinLinkMapper;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.SecureTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

        public CompanyJoinLinkService(
                CompanyJoinLinkRepository joinLinkRepository,
                CompanyRepository companyRepository,
                CompanyMembershipRepository membershipRepository,
                AppUserRepository appUserRepository,
                WorkerProfileRepository workerProfileRepository,
                SecureTokenService secureTokenService,
                InvitationProperties properties,
                AuthenticatedUserService authenticatedUserService,
                PasswordEncoder passwordEncoder
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
            Company company = findCompanyById(companyId);

            validateCompanyActive(company);

            AppUser currentUser =
                    authenticatedUserService.getCurrentUser();

            validateManagementPermission(
                    currentUser,
                    companyId
            );

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

        @Transactional(readOnly = true)
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

            return CompanyJoinLinkMapper.toResponseDTO(joinLink);
        }

        @Transactional(readOnly = true)
        public List<CompanyJoinLinkResponseDTO> findByCompany(
                Long companyId
        ) {
            validateCurrentUserCanManage(companyId);
            findCompanyById(companyId);

            return joinLinkRepository
                    .findByCompanyIdOrderByCreatedAtDesc(
                            companyId
                    )
                    .stream()
                    .map(CompanyJoinLinkMapper::toResponseDTO)
                    .toList();
        }

        @Transactional(readOnly = true)
        public List<CompanyJoinLinkResponseDTO> findByCompanyAndStatus(
                Long companyId,
                JoinLinkStatus status
        ) {
            validateCurrentUserCanManage(companyId);
            findCompanyById(companyId);

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
            validatePasswordConfirmation(request);

            String tokenHash =
                    hashRequiredToken(request.token());

            CompanyJoinLink joinLink =
                    joinLinkRepository
                            .findByTokenHashForUpdate(tokenHash)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Join link was not found."
                                    )
                            );

            validateJoinLinkCanBeUsed(joinLink);

            String normalizedEmail =
                    normalizeEmail(request.email());

            Optional<AppUser> existingUser =
                    appUserRepository
                            .findByEmailIgnoreCase(
                                    normalizedEmail
                            );

            AppUser appUser;
            boolean newAccountCreated;

            if (existingUser.isPresent()) {

                appUser = existingUser.get();

                validateExistingUser(
                        appUser,
                        request.password()
                );

                newAccountCreated = false;

            } else {

                appUser = createNewAppUser(
                        request,
                        normalizedEmail
                );

                newAccountCreated = true;
            }

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
                    newAccountCreated,
                    "COMPLETE_WORKER_PROFILE"
            );
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
                    findJoinLinkInCompany(
                            joinLinkId,
                            companyId
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
            if (
                    maxUses != null
                            && maxUses < 0
            ) {
                throw new BusinessException(
                        "Maximum uses cannot be negative."
                );
            }
        }

        private void validateExpirationDate(
                LocalDateTime expiresAt
        ) {
            if (
                    expiresAt != null
                            && !expiresAt.isAfter(
                            LocalDateTime.now()
                    )
            ) {
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

        private void validatePasswordConfirmation(
                AcceptCompanyJoinLinkRequestDTO request
        ) {
            if (
                    !request.password().equals(
                            request.confirmPassword()
                    )
            ) {
                throw new BusinessException(
                        "Password and password confirmation do not match."
                );
            }
        }

        private void validateExistingUser(
                AppUser appUser,
                String rawPassword
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
                            rawPassword,
                            appUser.getPassword()
                    )
            ) {
                throw new AccessDeniedBusinessException(
                        "Invalid email or password."
                );
            }
        }

        /*
         * ============================================================
         * USER CREATION
         * ============================================================
         */

        private AppUser createNewAppUser(
                AcceptCompanyJoinLinkRequestDTO request,
                String normalizedEmail
        ) {
            AppUser appUser = new AppUser();

            appUser.setName(
                    normalizeRequiredText(
                            request.name(),
                            "Name is required."
                    )
            );

            appUser.setSurname(
                    normalizeRequiredText(
                            request.surname(),
                            "Surname is required."
                    )
            );

            appUser.setEmail(normalizedEmail);

            appUser.setPassword(
                    passwordEncoder.encode(
                            request.password()
                    )
            );

            appUser.setStatus(
                    UserStatus.ACTIVE
            );

            return appUserRepository.save(appUser);
        }

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
            if (
                    workerProfileRepository
                            .existsByAppUserId(
                                    appUser.getId()
                            )
            ) {
                return;
            }

            WorkerProfile workerProfile =
                    new WorkerProfile();

            workerProfile.setAppUser(appUser);
            workerProfile.setStatus(
                    WorkerProfileStatus.INCOMPLETE
            );

            workerProfile.setGstRegistered(false);
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
                            .findById(joinLinkId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Company join link not found with ID: "
                                                    + joinLinkId
                                    )
                            );

            if (
                    !joinLink
                            .getCompany()
                            .getId()
                            .equals(companyId)
            ) {
                throw new ResourceNotFoundException(
                        "Company join link not found with ID: "
                                + joinLinkId
                );
            }

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

        /*
         * ============================================================
         * NORMALIZATION / URL
         * ============================================================
         */

        private Integer normalizeMaxUses(
                Integer maxUses
        ) {
            if (
                    maxUses == null
                            || maxUses == 0
            ) {
                return null;
            }

            return maxUses;
        }

        private Integer calculateRemainingUses(
                CompanyJoinLink joinLink
        ) {
            if (
                    joinLink.getMaxUses() == null
                            || joinLink.getMaxUses() == 0
            ) {
                return null;
            }

            int currentUses =
                    joinLink.getCurrentUses() == null
                            ? 0
                            : joinLink.getCurrentUses();

            return Math.max(
                    joinLink.getMaxUses()
                            - currentUses,
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