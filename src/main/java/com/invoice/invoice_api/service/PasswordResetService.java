package com.invoice.invoice_api.service;

import com.invoice.invoice_api.config.NotificationProperties;
import com.invoice.invoice_api.config.PasswordResetProperties;
import com.invoice.invoice_api.dto.auth.PasswordResetConfirmDTO;
import com.invoice.invoice_api.dto.auth.PasswordResetRequestDTO;
import com.invoice.invoice_api.dto.auth.PasswordResetResponseDTO;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.PasswordResetToken;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.PasswordResetTokenRepository;
import com.invoice.invoice_api.security.SecureTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String GENERIC_MESSAGE = "If an account exists for that email, a password reset link has been sent.";

    private final AppUserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenService secureTokens;
    private final PasswordResetProperties properties;
    private final NotificationProperties notificationProperties;
    private final ObjectProvider<JavaMailSender> mailSenders;

    public PasswordResetService(AppUserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder passwordEncoder, SecureTokenService secureTokens, PasswordResetProperties properties, NotificationProperties notificationProperties, ObjectProvider<JavaMailSender> mailSenders) {
        this.users = users; this.tokens = tokens; this.passwordEncoder = passwordEncoder; this.secureTokens = secureTokens; this.properties = properties; this.notificationProperties = notificationProperties; this.mailSenders = mailSenders;
    }

    @Transactional
    public PasswordResetResponseDTO request(PasswordResetRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) return new PasswordResetResponseDTO(GENERIC_MESSAGE, null);

        tokens.deleteByUserIdAndUsedAtIsNull(user.getId());
        String rawToken = secureTokens.generateToken();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUser(user); reset.setTokenHash(secureTokens.hashToken(rawToken));
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(properties.getExpirationMinutes()));
        tokens.save(reset);
        String url = resetUrl(rawToken);
        if (notificationProperties.isEmailEnabled()) sendEmail(user, url);
        else if (properties.isExposeDevelopmentLink()) log.warn("Password reset email disabled. Development reset link for {}: {}", user.getEmail(), url);
        else log.warn("Password reset email disabled; no reset link was delivered for {}.", user.getEmail());
        return new PasswordResetResponseDTO(GENERIC_MESSAGE, properties.isExposeDevelopmentLink() ? url : null);
    }

    @Transactional
    public void confirm(PasswordResetConfirmDTO request) {
        if (!request.password().equals(request.confirmPassword())) throw new BusinessException("Passwords must match.");
        PasswordResetToken reset = tokens.findByTokenHash(secureTokens.hashToken(request.token())).orElseThrow(() -> new BusinessException("This password reset link is invalid or expired."));
        if (reset.getUsedAt() != null || reset.getExpiresAt().isBefore(LocalDateTime.now())) throw new BusinessException("This password reset link is invalid or expired.");
        AppUser user = reset.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        reset.setUsedAt(LocalDateTime.now());
        tokens.save(reset);
        tokens.deleteByUserIdAndUsedAtIsNull(user.getId());
    }

    private String resetUrl(String rawToken) { return properties.getFrontendBaseUrl().replaceAll("/$", "") + "/reset-password?token=" + rawToken; }

    private void sendEmail(AppUser user, String url) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new IllegalStateException("Password reset email is enabled but no JavaMailSender is configured.");
        try {
            MimeMessageHelper helper = new MimeMessageHelper(sender.createMimeMessage(), false, "UTF-8");
            helper.setTo(user.getEmail()); helper.setSubject("Reset your Invoice Platform password");
            helper.setText("Use this link to reset your password (expires in " + properties.getExpirationMinutes() + " minutes):\n\n" + url, false);
            if (notificationProperties.getFrom() != null && !notificationProperties.getFrom().isBlank()) helper.setFrom(notificationProperties.getFrom());
            sender.send(helper.getMimeMessage());
        } catch (MessagingException error) { throw new IllegalStateException("Could not create password reset email.", error); }
    }
}
