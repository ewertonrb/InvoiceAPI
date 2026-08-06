package com.invoice.invoice_api.service;

import com.invoice.invoice_api.config.InvitationProperties;
import com.invoice.invoice_api.dto.joinLink.AcceptCompanyJoinLinkRequestDTO;
import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkRequestDTO;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyJoinLinkServiceTest {
    @Mock CompanyJoinLinkRepository links; @Mock CompanyRepository companies; @Mock CompanyMembershipRepository memberships; @Mock AppUserRepository users; @Mock WorkerProfileRepository profiles; @Mock SecureTokenService tokens; @Mock AuthenticatedUserService authenticated; @Mock PasswordEncoder passwords; @Mock CompanyContext context;
    CompanyJoinLinkService service;
    @BeforeEach void setup() { service = new CompanyJoinLinkService(links, companies, memberships, users, profiles, tokens, new InvitationProperties(), authenticated, passwords, context); }

    @Test void createRejectsNonWorkerRole() { stubManager(); assertThrows(BusinessException.class, () -> service.create(7L, new CompanyJoinLinkRequestDTO(CompanyRole.MANAGER, 2, LocalDateTime.now().plusDays(1)))); verifyNoInteractions(tokens, links); }
    @Test void createRequiresPositiveQuota() { stubManager(); assertThrows(BusinessException.class, () -> service.create(7L, new CompanyJoinLinkRequestDTO(CompanyRole.WORKER, 0, LocalDateTime.now().plusDays(1)))); verifyNoInteractions(tokens, links); }
    @Test void createRequiresFutureExpiry() { stubManager(); assertThrows(BusinessException.class, () -> service.create(7L, new CompanyJoinLinkRequestDTO(CompanyRole.WORKER, 2, LocalDateTime.now().minusMinutes(1)))); verifyNoInteractions(tokens, links); }

    @Test void acceptLocksEmailThenCompanyThenJoinLinkAndRejectsExhaustedQuota() {
        Company company = new Company(); company.setId(7L); company.setActive(true);
        CompanyJoinLink link = new CompanyJoinLink(); link.setCompany(company); link.setRole(CompanyRole.WORKER); link.setStatus(JoinLinkStatus.ACTIVE); link.setMaxUses(2); link.setCurrentUses(2); link.setExpiresAt(LocalDateTime.now().plusDays(1));
        AppUser user = mock(AppUser.class); when(user.getStatus()).thenReturn(UserStatus.ACTIVE); when(user.getEmail()).thenReturn("ALEX@example.com"); when(authenticated.getCurrentUser()).thenReturn(user);
        when(tokens.hashToken("raw")).thenReturn("hash"); when(links.findByTokenHash("hash")).thenReturn(Optional.of(link)); when(companies.findByIdForUpdate(7L)).thenReturn(Optional.of(company)); when(links.findByTokenHashForUpdate("hash")).thenReturn(Optional.of(link));

        assertThrows(InvalidOperationException.class, () -> service.accept(new AcceptCompanyJoinLinkRequestDTO("raw")));

        InOrder order = inOrder(links, companies);
        order.verify(links).findByTokenHash("hash");
        order.verify(links).acquireJoinEmailLock("alex@example.com");
        order.verify(companies).findByIdForUpdate(7L);
        order.verify(links).findByTokenHashForUpdate("hash");
        verifyNoInteractions(users, profiles);
        verify(links).save(link);
        org.junit.jupiter.api.Assertions.assertEquals(JoinLinkStatus.EXPIRED, link.getStatus());
    }

    private void stubManager() {
        AppUser user = mock(AppUser.class); when(user.getId()).thenReturn(1L);
        CompanyMembership membership = new CompanyMembership(); membership.setRole(CompanyRole.MANAGER); membership.setStatus(MembershipStatus.ACTIVE);
        Company company = new Company(); company.setId(7L); company.setActive(true);
        when(authenticated.getCurrentUser()).thenReturn(user); when(context.getCompanyId()).thenReturn(7L); when(memberships.findByAppUserIdAndCompanyId(1L, 7L)).thenReturn(Optional.of(membership)); when(companies.findById(7L)).thenReturn(Optional.of(company));
    }
}
