package com.invoice.invoice_api.security;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser appUser) {
        return buildToken(
                appUser,
                null,
                null
        );
    }

    public String generateCompanyToken(
            AppUser appUser,
            Long companyId,
            CompanyRole role
    ) {
        return buildToken(
                appUser,
                companyId,
                role
        );
    }

    private String buildToken(
            AppUser appUser,
            Long companyId,
            CompanyRole role
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        var builder = Jwts.builder()
                .subject(appUser.getEmail())
                .claim("userId", appUser.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration));

        if (companyId != null) {
            builder.claim("companyId", companyId);
        }

        if (role != null) {
            builder.claim("role", role.name());
        }

        return builder
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Number userId = extractAllClaims(token)
                .get("userId", Number.class);

        return userId == null
                ? null
                : userId.longValue();
    }

    public Long extractCompanyId(String token) {
        Number companyId = extractAllClaims(token)
                .get("companyId", Number.class);

        return companyId == null
                ? null
                : companyId.longValue();
    }

    public CompanyRole extractRole(String token) {
        String role = extractAllClaims(token)
                .get("role", String.class);

        return role == null
                ? null
                : CompanyRole.valueOf(role);
    }

    public boolean isTokenValid(
            String token,
            String userEmail
    ) {
        String tokenEmail = extractEmail(token);

        return tokenEmail.equalsIgnoreCase(userEmail)
                && !isTokenExpired(token);
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token)
                .getExpiration();

        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
