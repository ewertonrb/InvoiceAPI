package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.dto.platform.PlatformCompanyProvisionRequestDTO;
import com.invoice.invoice_api.dto.platform.PlatformOwnerRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.SystemRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.service.PlatformCompanyProvisioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformCompanyProvisioningServiceTest {
    @Mock CompanyRepository companies;
    @Mock AppUserRepository users;
    @Mock CompanyMembershipRepository memberships;
    @Mock AuthenticatedUserService authenticated;
    @Mock PasswordEncoder passwordEncoder;

    private PlatformCompanyProvisioningService service;

    @BeforeEach
    void setUp() {
        service = new PlatformCompanyProvisioningService(companies, users, memberships, authenticated, passwordEncoder);
    }

    @Test
    void provisionsCompanyAndActiveOwnerMembershipAtomically() throws Exception {
        AppUser admin = user(1L, SystemRole.PLATFORM_ADMIN);
        when(authenticated.getCurrentUser()).thenReturn(admin);
        when(companies.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(20L);
            return company;
        });
        when(passwordEncoder.encode("temporary-password")).thenReturn("encoded");
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser owner = invocation.getArgument(0);
            owner.setId(30L);
            return owner;
        });
        when(memberships.findByAppUserIdAndCompanyId(30L, 20L)).thenReturn(Optional.empty());
        when(memberships.save(any(CompanyMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.provision(request());

        assertTrue(response.ownerCreated());
        ArgumentCaptor<CompanyMembership> captor = ArgumentCaptor.forClass(CompanyMembership.class);
        verify(memberships).save(captor.capture());
        assertEquals(CompanyRole.OWNER, captor.getValue().getRole());
        assertEquals(MembershipStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(30L, captor.getValue().getAppUser().getId());
        assertEquals(20L, captor.getValue().getCompany().getId());
    }

    @Test
    void rejectsNonPlatformAdmin() {
        when(authenticated.getCurrentUser()).thenReturn(user(1L, SystemRole.USER));

        assertThrows(AccessDeniedBusinessException.class, () -> service.provision(request()));
        verifyNoInteractions(companies, users, memberships);
    }

    @Test
    void reusesExistingOwnerAndMembership() {
        AppUser admin = user(1L, SystemRole.PLATFORM_ADMIN);
        AppUser owner = user(30L, SystemRole.USER);
        Company company = company(20L);
        CompanyMembership existing = new CompanyMembership();
        when(authenticated.getCurrentUser()).thenReturn(admin);
        when(companies.save(any(Company.class))).thenReturn(company);
        when(users.findByEmailIgnoreCase("owner@example.test")).thenReturn(Optional.of(owner));
        when(memberships.findByAppUserIdAndCompanyId(30L, 20L)).thenReturn(Optional.of(existing));
        when(memberships.save(any(CompanyMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.provision(request());

        assertFalse(response.ownerCreated());
        verify(users, never()).save(any(AppUser.class));
        assertEquals(CompanyRole.OWNER, existing.getRole());
        assertEquals(MembershipStatus.ACTIVE, existing.getStatus());
    }

    @Test
    void isTransactionalForRollbackOfCompanyAndMembership() throws Exception {
        assertNotNull(PlatformCompanyProvisioningService.class
                .getMethod("provision", PlatformCompanyProvisionRequestDTO.class)
                .getAnnotation(Transactional.class));
    }

    private PlatformCompanyProvisionRequestDTO request() {
        return new PlatformCompanyProvisionRequestDTO(
                new CompanyRequestDTO("Pilot Co", "123", "company@example.test", null, null, true, true),
                new PlatformOwnerRequestDTO("Owner", "Person", "owner@example.test", "temporary-password")
        );
    }

    private AppUser user(Long id, SystemRole role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(id == 1L ? "admin@example.test" : "owner@example.test");
        user.setSystemRole(role);
        user.setStatus(com.invoice.invoice_api.enums.UserStatus.ACTIVE);
        return user;
    }

    private Company company(Long id) {
        Company company = new Company();
        company.setId(id);
        company.setName("Pilot Co");
        company.setAbn("123");
        company.setEmail("company@example.test");
        company.setActive(true);
        company.setContractorInvoiceGstEnabled(true);
        return company;
    }
}
