package com.invoice.invoice_api.security;

import com.invoice.invoice_api.exception.InvalidCredentialsException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticatedUserService {
    private final AppUserRepository appUserRepository;

    public AuthenticatedUserService(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || "anonymousUser".equals(
                        authentication.getPrincipal()
                )
        ) {
            throw new InvalidCredentialsException(
                    "Authenticated user not found"
            );
        }

        String email = authentication.getName();

        return appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Authenticated user not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
