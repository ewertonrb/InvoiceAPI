package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.dto.platform.PlatformCompanyProvisionRequestDTO;
import com.invoice.invoice_api.dto.platform.PlatformCompanyProvisionResponseDTO;
import com.invoice.invoice_api.dto.platform.PlatformOwnerRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.SystemRole;
import com.invoice.invoice_api.enums.UserStatus;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.CompanyMapper;
import com.invoice.invoice_api.mapper.CompanyMembershipMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlatformCompanyProvisioningService {
    private final CompanyRepository companyRepository;
    private final AppUserRepository appUserRepository;
    private final CompanyMembershipRepository membershipRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationEmailService notificationEmailService;

    public PlatformCompanyProvisioningService(
            CompanyRepository companyRepository,
            AppUserRepository appUserRepository,
            CompanyMembershipRepository membershipRepository,
            AuthenticatedUserService authenticatedUserService,
            PasswordEncoder passwordEncoder,
            NotificationEmailService notificationEmailService
    ) {
        this.companyRepository = companyRepository;
        this.appUserRepository = appUserRepository;
        this.membershipRepository = membershipRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.passwordEncoder = passwordEncoder;
        this.notificationEmailService = notificationEmailService;
    }

    @Transactional
    public PlatformCompanyProvisionResponseDTO provision(PlatformCompanyProvisionRequestDTO request) {
        requirePlatformAdmin();
        if (companyRepository.existsByAbn(request.company().abn())) {
            throw new DuplicateResourceException("Company ABN already exists.");
        }
        if (companyRepository.existsByEmail(request.company().email())) {
            throw new DuplicateResourceException("Company email already exists.");
        }

        Company company = CompanyMapper.toEntity(request.company());
        company.setActive(true);
        Company savedCompany = companyRepository.save(company);

        String ownerEmail = request.owner().email().trim().toLowerCase();
        AppUser owner = appUserRepository.findByEmailIgnoreCase(ownerEmail).orElse(null);
        boolean ownerCreated = owner == null;
        String temporaryPassword = null;
        if (ownerCreated) {
            temporaryPassword = request.owner().temporaryPassword();
            if (temporaryPassword == null || temporaryPassword.isBlank()) {
                throw new InvalidOperationException("temporaryPassword is required when creating a new owner account.");
            }
            owner = createOwner(request.owner(), ownerEmail, temporaryPassword);
        } else if (owner.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidOperationException("The owner account is not active.");
        }

        CompanyMembership membership = membershipRepository
                .findByAppUserIdAndCompanyId(owner.getId(), savedCompany.getId())
                .orElseGet(CompanyMembership::new);
        membership.setAppUser(owner);
        membership.setCompany(savedCompany);
        membership.setRole(CompanyRole.OWNER);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setAcceptedAt(LocalDateTime.now());
        membership.setRejectedAt(null);
        membership.setSuspendedAt(null);
        CompanyMembership savedMembership = membershipRepository.save(membership);

        notificationEmailService.sendOwnerSetupEmail(
                owner.getEmail(),
                owner.getFullName(),
                savedCompany.getName(),
                temporaryPassword
        );

        return new PlatformCompanyProvisionResponseDTO(
                CompanyMapper.toResponseDTO(savedCompany),
                CompanyMembershipMapper.toResponseDTO(savedMembership),
                ownerCreated
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> listAll() {
        requirePlatformAdmin();
        return companyRepository.findAll().stream()
                .map(CompanyMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(Long companyId) {
        requirePlatformAdmin();
        return companyRepository.findById(companyId)
                .map(CompanyMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));
    }

    @Transactional
    public CompanyResponseDTO setActive(Long companyId, boolean active) {
        requirePlatformAdmin();
        Company company = companyRepository.findByIdForUpdate(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));
        company.setActive(active);
        return CompanyMapper.toResponseDTO(companyRepository.save(company));
    }

    private AppUser createOwner(PlatformOwnerRequestDTO request, String email, String temporaryPassword) {
        AppUser owner = new AppUser();
        owner.setName(request.firstName().trim());
        owner.setSurname(request.lastName().trim());
        owner.setEmail(email);
        owner.setPassword(passwordEncoder.encode(temporaryPassword));
        owner.setStatus(UserStatus.ACTIVE);
        owner.setSystemRole(SystemRole.USER);
        return appUserRepository.save(owner);
    }

    private void requirePlatformAdmin() {
        if (authenticatedUserService.getCurrentUser().getSystemRole() != SystemRole.PLATFORM_ADMIN) {
            throw new AccessDeniedBusinessException("Platform administrator permission required.");
        }
    }
}
