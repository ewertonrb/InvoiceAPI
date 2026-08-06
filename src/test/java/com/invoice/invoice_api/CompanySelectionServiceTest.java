package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.auth.SelectCompanyRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.JwtService;
import com.invoice.invoice_api.service.CompanySelectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanySelectionServiceTest {
    @Mock AuthenticatedUserService authenticated;
    @Mock CompanyMembershipRepository memberships;
    @Mock JwtService jwt;

    @Test
    void rejectsForeignCompany() {
        CompanySelectionService service = new CompanySelectionService(authenticated, memberships, jwt);
        AppUser user = new AppUser();
        user.setId(7L);
        when(authenticated.getCurrentUser()).thenReturn(user);
        when(memberships.findByAppUserIdAndCompanyId(7L, 99L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedBusinessException.class,
                () -> service.selectCompany(new SelectCompanyRequestDTO(99L)));
        verifyNoInteractions(jwt);
    }

    @Test
    void rejectsInactiveCompanyMembership() {
        CompanySelectionService service = new CompanySelectionService(authenticated, memberships, jwt);
        AppUser user = new AppUser();
        user.setId(7L);
        Company company = new Company();
        company.setId(2L);
        company.setActive(true);
        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(company);
        membership.setStatus(MembershipStatus.SUSPENDED);
        membership.setRole(CompanyRole.OWNER);
        when(authenticated.getCurrentUser()).thenReturn(user);
        when(memberships.findByAppUserIdAndCompanyId(7L, 2L)).thenReturn(Optional.of(membership));

        assertThrows(AccessDeniedBusinessException.class,
                () -> service.selectCompany(new SelectCompanyRequestDTO(2L)));
        verify(jwt, never()).generateCompanyToken(any(), any(), any());
    }
}
