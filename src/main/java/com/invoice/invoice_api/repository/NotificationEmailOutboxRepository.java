package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.NotificationDeliveryStatus;
import com.invoice.invoice_api.model.NotificationEmailOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationEmailOutboxRepository extends JpaRepository<NotificationEmailOutbox, Long> {
    List<NotificationEmailOutbox> findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(NotificationDeliveryStatus status, LocalDateTime now, Pageable pageable);
}
