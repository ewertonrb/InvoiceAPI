package com.invoice.invoice_api;

import com.invoice.invoice_api.security.SecureTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecureTokenServiceTest {
    private SecureTokenService secureTokenService;

    @BeforeEach
    void setUp() {
        secureTokenService =
                new SecureTokenService();
    }

    @Test
    void shouldGenerateDifferentTokens() {
        String firstToken =
                secureTokenService.generateToken();

        String secondToken =
                secureTokenService.generateToken();

        assertNotNull(firstToken);
        assertNotNull(secondToken);

        assertFalse(firstToken.isBlank());
        assertFalse(secondToken.isBlank());

        assertNotEquals(
                firstToken,
                secondToken
        );
    }

    @Test
    void shouldHashTokenConsistently() {
        String rawToken =
                "example-token";

        String firstHash =
                secureTokenService.hashToken(rawToken);

        String secondHash =
                secureTokenService.hashToken(rawToken);

        assertEquals(
                firstHash,
                secondHash
        );

        assertEquals(
                64,
                firstHash.length()
        );
    }

    @Test
    void shouldMatchRawTokenWithHash() {
        String rawToken =
                secureTokenService.generateToken();

        String hash =
                secureTokenService.hashToken(rawToken);

        assertTrue(
                secureTokenService.matches(
                        rawToken,
                        hash
                )
        );
    }

    @Test
    void shouldNotMatchDifferentToken() {
        String firstToken =
                secureTokenService.generateToken();

        String secondToken =
                secureTokenService.generateToken();

        String firstTokenHash =
                secureTokenService.hashToken(
                        firstToken
                );

        assertFalse(
                secureTokenService.matches(
                        secondToken,
                        firstTokenHash
                )
        );
    }
}
