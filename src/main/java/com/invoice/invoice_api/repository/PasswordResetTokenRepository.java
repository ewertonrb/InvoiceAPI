package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUserIdAndUsedAtIsNull(Long userId);
}
