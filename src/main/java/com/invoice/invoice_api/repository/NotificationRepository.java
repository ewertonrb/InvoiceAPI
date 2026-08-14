package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdAndCompanyIdOrderByCreatedAtDesc(Long userId, Long companyId, Pageable pageable);
    long countByUserIdAndCompanyIdAndReadAtIsNull(Long userId, Long companyId);
    @Modifying
    @Query("update Notification n set n.readAt = CURRENT_TIMESTAMP where n.id = :id and n.user.id = :userId and n.company.id = :companyId and n.readAt is null")
    int markRead(@Param("id") Long id, @Param("userId") Long userId, @Param("companyId") Long companyId);
    @Modifying
    @Query("update Notification n set n.readAt = CURRENT_TIMESTAMP where n.user.id = :userId and n.company.id = :companyId and n.readAt is null")
    int markAllRead(@Param("userId") Long userId, @Param("companyId") Long companyId);
}
