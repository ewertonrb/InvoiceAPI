package com.invoice.invoice_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class JoinLinkTokenCipher {
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public JoinLinkTokenCipher(@Value("${security.jwt.secret}") String secret) {
        try { this.key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception exception) { throw new IllegalStateException("Could not initialize join-link protection.", exception); }
    }

    public String encrypt(String token) {
        try {
            byte[] iv = new byte[12]; random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length); System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (Exception exception) { throw new IllegalStateException("Could not protect join-link token.", exception); }
    }

    public String decrypt(String value) {
        try {
            byte[] payload = Base64.getUrlDecoder().decode(value); byte[] iv = java.util.Arrays.copyOf(payload, 12); byte[] encrypted = java.util.Arrays.copyOfRange(payload, 12, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) { throw new IllegalStateException("Could not recover join-link token.", exception); }
    }
}
