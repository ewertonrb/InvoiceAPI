package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.login.LoginRequestDTO;
import com.invoice.invoice_api.dto.login.LoginResponseDTO;
import com.invoice.invoice_api.exception.InvalidCredentialsException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String normalizedEmail = normalizeEmail(request.email());

        AppUser appUser = appUserRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        if (!Boolean.TRUE.equals(appUser.getActive())) {
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(
                request.password(),
                appUser.getPassword()
        )) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(appUser);

        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                appUser.getId(),
                appUser.getName(),
                appUser.getEmail()
        );
    }

    private InvalidCredentialsException invalidCredentials() {
        return new InvalidCredentialsException(
                "Invalid email or password"
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
