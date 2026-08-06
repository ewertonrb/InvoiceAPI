package com.invoice.invoice_api.security;

import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

        private final JwtService jwtService;
        private final AppUserDetailsService appUserDetailsService;
        private final CompanyContext companyContext;
        private final CompanyMembershipRepository companyMembershipRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AppUserDetailsService appUserDetailsService,
            CompanyContext companyContext,
            CompanyMembershipRepository companyMembershipRepository
    ) {
        this.jwtService = jwtService;
        this.appUserDetailsService = appUserDetailsService;
        this.companyContext = companyContext;
        this.companyMembershipRepository = companyMembershipRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null) {
                if (!authenticateToken(token, response)) {
                    companyContext.clear();
                    return;
                }
            }

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
            rejectUnauthorized(response);
            companyContext.clear();
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            companyContext.clear();
        }
    }

    private boolean authenticateToken(
            String token,
            HttpServletResponse response
    ) throws IOException {
        String email = jwtService.extractEmail(token);

        boolean isNotAuthenticated = SecurityContextHolder
                .getContext()
                .getAuthentication() == null;

        if (email == null || !isNotAuthenticated) {
            return true;
        }

        UserDetails userDetails = appUserDetailsService
                .loadUserByUsername(email);

        if (!userDetails.isEnabled()
                || !jwtService.isTokenValid(token, userDetails.getUsername())) {
            rejectUnauthorized(response);
            return false;
        }

                    Long companyId = jwtService.extractCompanyId(token);
                    var tokenRole = jwtService.extractRole(token);

                    if ((companyId == null) != (tokenRole == null)) {
                        rejectUnauthorized(response);
                        return false;
                    }

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    userDetails.getAuthorities().stream()
                            .filter(authority -> "PLATFORM_ADMIN".equals(authority.getAuthority()))
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .forEach(authorities::add);

                    if (companyId != null) {
                        Long userId = jwtService.extractUserId(token);
                        CompanyMembership membership = userId == null
                                ? null
                                : companyMembershipRepository
                                .findByAppUserIdAndCompanyIdAndStatus(
                                        userId,
                                        companyId,
                                        MembershipStatus.ACTIVE
                                )
                                .orElse(null);

                        if (membership == null
                                || !Boolean.TRUE.equals(membership.getCompany().getActive())
                                || membership.getRole() != tokenRole) {
                            rejectUnauthorized(response);
                            return false;
                        }

                        authorities.add(new SimpleGrantedAuthority(tokenRole.name()));
                        companyContext.set(companyId, tokenRole);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
        return true;
    }

    private void rejectUnauthorized(HttpServletResponse response)
            throws IOException {
        SecurityContextHolder.clearContext();
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or inactive authentication session"
        );
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader =
                request.getHeader("Authorization");

        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }
}
