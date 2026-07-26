package com.invoice.invoice_api.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

        private final JwtService jwtService;
        private final AppUserDetailsService appUserDetailsService;
        private final CompanyContext companyContext;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AppUserDetailsService appUserDetailsService,
            CompanyContext companyContext
    ) {
        this.jwtService = jwtService;
        this.appUserDetailsService = appUserDetailsService;
        this.companyContext = companyContext;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);

            boolean isNotAuthenticated =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null;

            if (email != null && isNotAuthenticated) {

                UserDetails userDetails =
                        appUserDetailsService
                                .loadUserByUsername(email);

                if (!userDetails.isEnabled()) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtService.isTokenValid(
                        token,
                        userDetails.getUsername()
                )) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    Long companyId =
                            jwtService.extractCompanyId(token);

                    var role =
                            jwtService.extractRole(token);

                    if (companyId != null && role != null) {
                        companyContext.set(
                                companyId,
                                role
                        );
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();

            filterChain.doFilter(request, response);

        } finally {
            companyContext.clear();
        }
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
