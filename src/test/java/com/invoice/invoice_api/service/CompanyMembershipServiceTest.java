package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRoleRequestDTO;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.*;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.CompanyContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyMembershipServiceTest {
    @Mock CompanyMembershipRepository memberships; @Mock AppUserRepository users; @Mock CompanyRepository companies; @Mock WorkerProfileRepository profiles; @Mock CompanyContext context;
    CompanyMembershipService service;
    @BeforeEach void setup() { service = new CompanyMembershipService(memberships, users, companies, profiles, context); }

    @ParameterizedTest @EnumSource(value = CompanyRole.class, names = {"MANAGER", "FINANCE", "WORKER"})
    void onlyOwnerAndAdminCanChangeRoles(CompanyRole actorRole) {
        when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(actorRole);
        assertThrows(AccessDeniedBusinessException.class, () -> service.updateRole(7L, 4L, request(CompanyRole.MANAGER)));
        verifyNoInteractions(memberships, profiles);
    }

    @Test void companyMismatchIsRejectedBeforeRoleOrLookup() {
        when(context.getCompanyId()).thenReturn(8L);
        assertThrows(AccessDeniedBusinessException.class, () -> service.updateRole(7L, 4L, request(CompanyRole.MANAGER)));
        verify(context, never()).getRole(); verifyNoInteractions(memberships);
    }

    @ParameterizedTest @EnumSource(value = CompanyRole.class, names = {"OWNER", "ADMIN"})
    void ownerAndAdminCanPromoteActiveWorkerToManager(CompanyRole actorRole) {
        Fixture fixture = fixture(CompanyRole.WORKER, MembershipStatus.ACTIVE, 7L);
        when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(actorRole);
        when(memberships.findById(4L)).thenReturn(Optional.of(fixture.membership)); when(profiles.findByAppUserId(2L)).thenReturn(Optional.empty()); when(memberships.findByIdAndCompanyIdForUpdate(4L, 7L)).thenReturn(Optional.of(fixture.membership)); when(memberships.findByAppUserId(2L)).thenReturn(List.of(fixture.membership)); when(memberships.save(fixture.membership)).thenReturn(fixture.membership);
        service.updateRole(7L, 4L, request(CompanyRole.MANAGER));
        assertEquals(CompanyRole.MANAGER, fixture.membership.getRole()); verify(memberships).findByIdAndCompanyIdForUpdate(4L, 7L);
    }

    @Test void inactiveTargetCannotChangeRole() {
        Fixture fixture = fixture(CompanyRole.WORKER, MembershipStatus.SUSPENDED, 7L); stubOwnerTarget(fixture);
        assertThrows(InvalidOperationException.class, () -> service.updateRole(7L, 4L, request(CompanyRole.MANAGER)));
        verify(memberships, never()).save(any());
    }

    @Test void ownerAndAdminTargetsAndWorkerDestinationAreForbidden() {
        for (CompanyRole source : List.of(CompanyRole.OWNER, CompanyRole.ADMIN)) {
            reset(memberships, profiles, context); Fixture fixture = fixture(source, MembershipStatus.ACTIVE, 7L); stubOwnerTarget(fixture);
            assertThrows(InvalidOperationException.class, () -> service.updateRole(7L, 4L, request(CompanyRole.MANAGER)));
        }
        reset(memberships, profiles, context); Fixture fixture = fixture(CompanyRole.MANAGER, MembershipStatus.ACTIVE, 7L); stubOwnerTarget(fixture);
        assertThrows(InvalidOperationException.class, () -> service.updateRole(7L, 4L, request(CompanyRole.WORKER)));
    }

    @Test void promotionSuspendsProfileOnlyWhenLastActiveWorkerMembershipIsRemoved() {
        Fixture fixture = fixture(CompanyRole.WORKER, MembershipStatus.ACTIVE, 7L); WorkerProfile profile = new WorkerProfile(); profile.setId(10L); profile.setAppUser(fixture.user); profile.setStatus(WorkerProfileStatus.COMPLETE);
        when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.OWNER); when(memberships.findById(4L)).thenReturn(Optional.of(fixture.membership)); when(profiles.findByAppUserId(2L)).thenReturn(Optional.of(profile)); when(profiles.findByIdForUpdate(10L)).thenReturn(Optional.of(profile)); when(memberships.findByIdAndCompanyIdForUpdate(4L, 7L)).thenReturn(Optional.of(fixture.membership)); when(memberships.findByAppUserId(2L)).thenReturn(List.of(fixture.membership)); when(memberships.save(fixture.membership)).thenReturn(fixture.membership);
        service.updateRole(7L, 4L, request(CompanyRole.FINANCE));
        assertEquals(WorkerProfileStatus.SUSPENDED, profile.getStatus()); verify(profiles).save(profile);
    }

    @Test void anotherActiveWorkerMembershipPreservesProfile() {
        Fixture fixture = fixture(CompanyRole.WORKER, MembershipStatus.ACTIVE, 7L); Fixture other = fixture(CompanyRole.WORKER, MembershipStatus.ACTIVE, 8L); WorkerProfile profile = new WorkerProfile(); profile.setId(10L); profile.setAppUser(fixture.user); profile.setStatus(WorkerProfileStatus.COMPLETE);
        when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.ADMIN); when(memberships.findById(4L)).thenReturn(Optional.of(fixture.membership)); when(profiles.findByAppUserId(2L)).thenReturn(Optional.of(profile)); when(profiles.findByIdForUpdate(10L)).thenReturn(Optional.of(profile)); when(memberships.findByIdAndCompanyIdForUpdate(4L, 7L)).thenReturn(Optional.of(fixture.membership)); when(memberships.findByAppUserId(2L)).thenReturn(List.of(fixture.membership, other.membership)); when(memberships.save(fixture.membership)).thenReturn(fixture.membership);
        service.updateRole(7L, 4L, request(CompanyRole.MANAGER));
        assertEquals(WorkerProfileStatus.COMPLETE, profile.getStatus()); verify(profiles, never()).save(profile);
    }

    @Test void legacyMembershipReadIsCompanyScopedAndDeniedToFinance() {
        when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.FINANCE);
        assertThrows(AccessDeniedBusinessException.class, () -> service.findById(4L)); verifyNoInteractions(memberships);
        reset(context, memberships); when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.MANAGER); Fixture foreign = fixture(CompanyRole.WORKER, MembershipStatus.ACTIVE, 8L); when(memberships.findById(4L)).thenReturn(Optional.of(foreign.membership));
        assertThrows(ResourceNotFoundException.class, () -> service.findById(4L));
    }

    private void stubOwnerTarget(Fixture fixture) { when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.OWNER); when(memberships.findById(4L)).thenReturn(Optional.of(fixture.membership)); when(profiles.findByAppUserId(2L)).thenReturn(Optional.empty()); when(memberships.findByIdAndCompanyIdForUpdate(4L, 7L)).thenReturn(Optional.of(fixture.membership)); }
    private CompanyMembershipRoleRequestDTO request(CompanyRole role) { return new CompanyMembershipRoleRequestDTO(role); }
    private Fixture fixture(CompanyRole role, MembershipStatus status, Long companyId) { AppUser user = new AppUser(); user.setId(2L); user.setName("Alex"); user.setEmail("a@example.com"); Company company = new Company(); company.setId(companyId); company.setName("Company"); CompanyMembership membership = new CompanyMembership(); membership.setAppUser(user); membership.setCompany(company); membership.setRole(role); membership.setStatus(status); return new Fixture(user, membership); }
    private record Fixture(AppUser user, CompanyMembership membership) {}
}
