package com.invoice.invoice_api.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class SecureTokenService {
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom;

    public SecureTokenService() {
        this.secureRandom = new SecureRandom();
    }

    public String generateToken() {
        byte[] tokenBytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Token is required."
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }

    public boolean matches(
            String rawToken,
            String expectedHash
    ) {
        if (
                rawToken == null
                        || expectedHash == null
        ) {
            return false;
        }

        String actualHash =
                hashToken(rawToken);

        return MessageDigest.isEqual(
                actualHash.getBytes(
                        StandardCharsets.UTF_8
                ),
                expectedHash.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}
