package com.invoice.invoice_api.service;

import com.invoice.invoice_api.config.NotificationProperties;
import com.invoice.invoice_api.enums.NotificationDeliveryStatus;
import com.invoice.invoice_api.model.NotificationEmailOutbox;
import com.invoice.invoice_api.repository.NotificationEmailOutboxRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class NotificationEmailService {

    private final NotificationEmailOutboxRepository outbox;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final NotificationProperties properties;

    public NotificationEmailService(NotificationEmailOutboxRepository outbox, ObjectProvider<JavaMailSender> mailSenders, NotificationProperties properties) { this.outbox = outbox; this.mailSenders = mailSenders; this.properties = properties; }

    @Scheduled(fixedDelayString = "${app.notifications.poll-interval-ms:30000}")
    @Transactional
    public void processPending() {
        if (!properties.isEmailEnabled()) return;
        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null) throw new IllegalStateException("Email notifications are enabled but no JavaMailSender is configured.");
        for (NotificationEmailOutbox item : outbox.findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(NotificationDeliveryStatus.PENDING, LocalDateTime.now(), PageRequest.of(0, 25))) send(item, mailSender);
    }

    private void send(NotificationEmailOutbox item, JavaMailSender mailSender) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(item.getRecipient()); helper.setSubject(item.getSubject()); helper.setText(item.getBody(), false);
            if (properties.getFrom() != null && !properties.getFrom().isBlank()) helper.setFrom(properties.getFrom());
            mailSender.send(message); item.setStatus(NotificationDeliveryStatus.SENT); item.setSentAt(LocalDateTime.now()); item.setLastError(null);
        } catch (MessagingException | RuntimeException error) {
            int attempts = item.getAttempts() + 1; item.setAttempts(attempts); item.setLastError(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
            if (attempts >= properties.getMaxAttempts()) item.setStatus(NotificationDeliveryStatus.FAILED); else item.setNextAttemptAt(LocalDateTime.now().plusMinutes(Math.min(60, 1L << Math.min(attempts, 6))));
        }
        outbox.save(item);
    }
}
