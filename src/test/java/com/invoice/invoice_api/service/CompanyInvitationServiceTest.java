package com.invoice.invoice_api.service;

import com.invoice.invoice_api.config.InvitationProperties;
import com.invoice.invoice_api.dto.companyInvitation.DeclineCompanyInvitationRequestDTO;
import com.invoice.invoice_api.enums.InvitationStatus;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyInvitation;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyInvitationServiceTest {
    @Mock CompanyInvitationRepository invitations; @Mock CompanyRepository companies; @Mock AppUserRepository users; @Mock WorkerProfileRepository profiles; @Mock PasswordEncoder passwords; @Mock CompanyMembershipRepository memberships; @Mock SecureTokenService tokens; @Mock AuthenticatedUserService authenticated; @Mock CompanyContext context;
    @Mock NotificationEmailService notificationEmailService;
    CompanyInvitationService service;
    @BeforeEach void setup() { service = new CompanyInvitationService(invitations, companies, users, profiles, passwords, memberships, tokens, new InvitationProperties(), authenticated, context, notificationEmailService); }

    @Test void declineUsesTokenHashPessimisticLock() {
        CompanyInvitation invitation = pendingInvitation(); stubLockedLookup("raw-token", "hash", invitation);
        service.decline(new DeclineCompanyInvitationRequestDTO(" raw-token "));
        verify(invitations).findByTokenHashForUpdate("hash"); verify(invitations).save(invitation); org.junit.jupiter.api.Assertions.assertEquals(InvitationStatus.DECLINED, invitation.getStatus());
    }

    @Test void acceptedInvitationCannotBeDeclinedAndIsNotSaved() {
        CompanyInvitation invitation = pendingInvitation(); invitation.setStatus(InvitationStatus.ACCEPTED); stubLockedLookup("raw", "hash", invitation);
        assertThrows(InvalidOperationException.class, () -> service.decline(new DeclineCompanyInvitationRequestDTO("raw")));
        verify(invitations, never()).save(any());
    }

    @Test void repeatedDeclineIsIdempotent() {
        CompanyInvitation invitation = pendingInvitation(); invitation.setStatus(InvitationStatus.DECLINED); stubLockedLookup("raw", "hash", invitation);
        service.decline(new DeclineCompanyInvitationRequestDTO("raw"));
        verify(invitations, never()).save(any());
    }

    private CompanyInvitation pendingInvitation() { Company company = new Company(); company.setId(7L); CompanyInvitation invitation = new CompanyInvitation(); invitation.setCompany(company); invitation.setEmail("worker@example.com"); invitation.setStatus(InvitationStatus.PENDING); invitation.setExpiresAt(LocalDateTime.now().plusDays(1)); return invitation; }
    private void stubLockedLookup(String raw, String hash, CompanyInvitation invitation) { when(tokens.hashToken(raw)).thenReturn(hash); when(invitations.findByTokenHash(hash)).thenReturn(Optional.of(invitation)); when(companies.findByIdForUpdate(7L)).thenReturn(Optional.of(invitation.getCompany())); when(invitations.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(invitation)); }
}
