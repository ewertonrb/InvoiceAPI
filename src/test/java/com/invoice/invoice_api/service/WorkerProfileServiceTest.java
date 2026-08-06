package com.invoice.invoice_api.service;

import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileServiceTest {
    @Mock WorkerProfileRepository profiles; @Mock CompanyMembershipRepository memberships; @Mock AuthenticatedUserService users; @Mock WorkerProfileValidator validator; @Mock CompanyContext context;
    WorkerProfileService service;
    @BeforeEach void setup() { service = new WorkerProfileService(profiles, memberships, users, validator, context); }

    @Test void rejectsRequestedCompanyDifferentFromSelectedCompanyBeforeUserLookup() {
        when(context.getCompanyId()).thenReturn(7L);
        assertThrows(AccessDeniedBusinessException.class, () -> service.findActiveWorkersByCompany(8L));
        verifyNoInteractions(users, memberships, profiles);
    }

    @Test void financeCannotManageWorkers() {
        AppUser admin = mock(AppUser.class); CompanyMembership membership = membership(CompanyRole.FINANCE, MembershipStatus.ACTIVE);
        when(context.getCompanyId()).thenReturn(7L); when(users.getCurrentUser()).thenReturn(admin); when(admin.getId()).thenReturn(1L); when(memberships.findByAppUserIdAndCompanyId(1L, 7L)).thenReturn(Optional.of(membership));
        assertThrows(AccessDeniedBusinessException.class, () -> service.findActiveWorkersByCompany(7L));
        verifyNoInteractions(profiles);
    }

    @Test void suspendingLastActiveWorkerMembershipSuspendsGlobalProfile() {
        AppUser admin = mock(AppUser.class), workerUser = mock(AppUser.class); when(admin.getId()).thenReturn(1L); when(workerUser.getId()).thenReturn(2L); when(workerUser.getFullName()).thenReturn("Worker"); when(workerUser.getEmail()).thenReturn("w@example.com");
        CompanyMembership adminMembership = membership(CompanyRole.MANAGER, MembershipStatus.ACTIVE); CompanyMembership workerMembership = membership(CompanyRole.WORKER, MembershipStatus.ACTIVE);
        WorkerProfile profile = new WorkerProfile(); profile.setId(4L); profile.setAppUser(workerUser); profile.setStatus(WorkerProfileStatus.COMPLETE);
        when(context.getCompanyId()).thenReturn(7L); when(users.getCurrentUser()).thenReturn(admin); when(memberships.findByAppUserIdAndCompanyId(1L, 7L)).thenReturn(Optional.of(adminMembership)); when(profiles.findByIdForUpdate(4L)).thenReturn(Optional.of(profile)); when(memberships.findByAppUserIdAndCompanyId(2L, 7L)).thenReturn(Optional.of(workerMembership)); when(memberships.findByAppUserId(2L)).thenReturn(List.of(workerMembership)); when(profiles.save(profile)).thenReturn(profile);
        service.suspend(7L, 4L);
        verify(memberships).save(workerMembership); verify(profiles).save(profile);
        org.junit.jupiter.api.Assertions.assertEquals(MembershipStatus.SUSPENDED, workerMembership.getStatus()); org.junit.jupiter.api.Assertions.assertEquals(WorkerProfileStatus.SUSPENDED, profile.getStatus());
    }

    @Test void suspendingOneCompanyMembershipPreservesProfileWhenAnotherIsActive() {
        AppUser admin = mock(AppUser.class), workerUser = mock(AppUser.class); Company otherCompany = mock(Company.class);
        when(admin.getId()).thenReturn(1L); when(workerUser.getId()).thenReturn(2L); when(workerUser.getFullName()).thenReturn("Worker"); when(workerUser.getEmail()).thenReturn("w@example.com"); when(otherCompany.getId()).thenReturn(8L);
        CompanyMembership adminMembership = membership(CompanyRole.OWNER, MembershipStatus.ACTIVE), current = membership(CompanyRole.WORKER, MembershipStatus.ACTIVE), other = membership(CompanyRole.WORKER, MembershipStatus.ACTIVE); other.setCompany(otherCompany);
        WorkerProfile profile = new WorkerProfile(); profile.setId(4L); profile.setAppUser(workerUser); profile.setStatus(WorkerProfileStatus.COMPLETE);
        when(context.getCompanyId()).thenReturn(7L); when(users.getCurrentUser()).thenReturn(admin); when(memberships.findByAppUserIdAndCompanyId(1L, 7L)).thenReturn(Optional.of(adminMembership)); when(profiles.findByIdForUpdate(4L)).thenReturn(Optional.of(profile)); when(memberships.findByAppUserIdAndCompanyId(2L, 7L)).thenReturn(Optional.of(current)); when(memberships.findByAppUserId(2L)).thenReturn(List.of(current, other)); when(profiles.save(profile)).thenReturn(profile);
        service.suspend(7L, 4L);
        org.junit.jupiter.api.Assertions.assertEquals(WorkerProfileStatus.COMPLETE, profile.getStatus());
    }

    @Test void suspendedFilterUsesExactMembershipStatusForBothQueries() {
        AppUser admin = mock(AppUser.class); CompanyMembership adminMembership = membership(CompanyRole.ADMIN, MembershipStatus.ACTIVE);
        when(admin.getId()).thenReturn(1L); when(context.getCompanyId()).thenReturn(7L); when(users.getCurrentUser()).thenReturn(admin); when(memberships.findByAppUserIdAndCompanyId(1L, 7L)).thenReturn(Optional.of(adminMembership));
        when(memberships.findByCompanyIdAndStatusIn(7L, List.of(MembershipStatus.SUSPENDED))).thenReturn(List.of());
        when(profiles.findWorkersByCompanyIdAndMembershipStatuses(7L, List.of(MembershipStatus.SUSPENDED))).thenReturn(List.of());

        service.findWorkersByCompany(7L, true, MembershipStatus.SUSPENDED);

        verify(memberships).findByCompanyIdAndStatusIn(7L, List.of(MembershipStatus.SUSPENDED));
        verify(profiles).findWorkersByCompanyIdAndMembershipStatuses(7L, List.of(MembershipStatus.SUSPENDED));
    }

    private CompanyMembership membership(CompanyRole role, MembershipStatus status) { CompanyMembership value = new CompanyMembership(); value.setRole(role); value.setStatus(status); return value; }
}
