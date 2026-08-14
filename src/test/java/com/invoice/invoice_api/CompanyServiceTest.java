package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock CompanyMembershipRepository membershipRepository;
    @Mock AuthenticatedUserService authenticatedUserService;
    @Mock CompanyContext companyContext;

    private CompanyService service;

    @BeforeEach
    void setUp() {
        service = new CompanyService(
                companyRepository,
                membershipRepository,
                authenticatedUserService,
                companyContext
        );
    }

    @Test
    void findAllReturnsOnlyCompaniesFromCurrentUsersActiveMemberships() {
        Company accessible = company(11L, true);
        when(authenticatedUserService.getCurrentUserId()).thenReturn(7L);
        when(membershipRepository.findByAppUserIdAndStatus(7L, MembershipStatus.ACTIVE))
                .thenReturn(List.of(membership(accessible)));

        var result = service.findAll();

        assertEquals(List.of(11L), result.stream().map(response -> response.id()).toList());
        verify(companyRepository, never()).findAll();
    }

    @Test
    void findByIdHidesCompanyWithoutActiveMembership() {
        when(authenticatedUserService.getCurrentUserId()).thenReturn(7L);
        when(membershipRepository.findByAppUserIdAndCompanyIdAndStatus(
                7L, 99L, MembershipStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateRejectsCrossCompanyRequestBeforeLoadingCompany() {
        when(companyContext.getCompanyId()).thenReturn(11L);

        assertThrows(AccessDeniedBusinessException.class,
                () -> service.update(99L, request("123", "owner@example.test")));

        verify(authenticatedUserService, never()).getCurrentUserId();
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deactivateRejectsRoleWithoutCompanyManagementPermission() {
        when(companyContext.getCompanyId()).thenReturn(11L);
        when(companyContext.getRole()).thenReturn(CompanyRole.FINANCE);

        assertThrows(AccessDeniedBusinessException.class, () -> service.delete(11L));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void managerCannotDeactivateCompany() {
        when(companyContext.getCompanyId()).thenReturn(11L);
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);

        assertThrows(AccessDeniedBusinessException.class, () -> service.delete(11L));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void ownerCannotDeactivateCompany() {
        when(companyContext.getCompanyId()).thenReturn(11L);
        when(companyContext.getRole()).thenReturn(CompanyRole.OWNER);

        assertThrows(AccessDeniedBusinessException.class, () -> service.delete(11L));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void administratorCanDeactivateAccessibleSelectedCompany() {
        Company company = company(11L, true);
        when(companyContext.getCompanyId()).thenReturn(11L);
        when(companyContext.getRole()).thenReturn(CompanyRole.ADMIN);
        when(authenticatedUserService.getCurrentUserId()).thenReturn(7L);
        when(membershipRepository.findByAppUserIdAndCompanyIdAndStatus(
                7L, 11L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(membership(company)));

        service.delete(11L);

        assertFalse(company.getActive());
        verify(companyRepository).save(company);
    }

    private CompanyRequestDTO request(String abn, String email) {
        return new CompanyRequestDTO(
                "Acme", abn, email, null, null, false, true
        );
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        return user;
    }

    private Company company(Long id, boolean active) {
        Company company = new Company();
        company.setId(id);
        company.setName("Acme");
        company.setAbn("123");
        company.setEmail("owner@example.test");
        company.setActive(active);
        company.setContractorInvoiceGstEnabled(false);
        return company;
    }

    private CompanyMembership membership(Company company) {
        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(company);
        membership.setStatus(MembershipStatus.ACTIVE);
        return membership;
    }
}
