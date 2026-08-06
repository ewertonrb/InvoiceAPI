package com.invoice.invoice_api.security;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtService jwtService;
    @Mock AppUserDetailsService appUserDetailsService;
    @Mock CompanyContext companyContext;
    @Mock CompanyMembershipRepository membershipRepository;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsCompanyTokenWhenMembershipCompanyIsInactive() throws Exception {
        String token = "company-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn("user@example.test");
        when(appUserDetailsService.loadUserByUsername("user@example.test"))
                .thenReturn(User.withUsername("user@example.test")
                        .password("password")
                        .authorities("BASE")
                        .build());
        when(jwtService.isTokenValid(token, "user@example.test")).thenReturn(true);
        when(jwtService.extractCompanyId(token)).thenReturn(11L);
        when(jwtService.extractRole(token)).thenReturn(CompanyRole.OWNER);
        when(jwtService.extractUserId(token)).thenReturn(7L);

        Company inactiveCompany = new Company();
        inactiveCompany.setActive(false);
        CompanyMembership membership = new CompanyMembership();
        membership.setCompany(inactiveCompany);
        membership.setRole(CompanyRole.OWNER);
        membership.setStatus(MembershipStatus.ACTIVE);
        when(membershipRepository.findByAppUserIdAndCompanyIdAndStatus(
                7L, 11L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(membership));

        filter().invoke(request, response, chain);

        verify(response).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or inactive authentication session"
        );
        verify(chain, never()).doFilter(request, response);
        verify(companyContext).clear();
    }

    private ExposedJwtAuthenticationFilter filter() {
        return new ExposedJwtAuthenticationFilter(
                jwtService,
                appUserDetailsService,
                companyContext,
                membershipRepository
        );
    }

    private static class ExposedJwtAuthenticationFilter
            extends JwtAuthenticationFilter {
        ExposedJwtAuthenticationFilter(
                JwtService jwtService,
                AppUserDetailsService appUserDetailsService,
                CompanyContext companyContext,
                CompanyMembershipRepository membershipRepository
        ) {
            super(jwtService, appUserDetailsService, companyContext, membershipRepository);
        }

        void invoke(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain chain
        ) throws ServletException, IOException {
            doFilterInternal(request, response, chain);
        }
    }
}
