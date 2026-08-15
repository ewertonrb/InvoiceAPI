package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.notification.NotificationResponseDTO;
import com.invoice.invoice_api.enums.*;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final NotificationEmailOutboxRepository outbox;
    private final CompanyRepository companies;
    private final CompanyContext context;
    private final AuthenticatedUserService auth;

    public NotificationService(NotificationRepository notifications, NotificationEmailOutboxRepository outbox, CompanyRepository companies, CompanyContext context, AuthenticatedUserService auth) {
        this.notifications = notifications; this.outbox = outbox; this.companies = companies; this.context = context; this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> list(Long companyId, int limit) {
        requireCompany(companyId);
        return notifications.findByUserIdAndCompanyIdOrderByCreatedAtDesc(auth.getCurrentUserId(), companyId, PageRequest.of(0, Math.min(Math.max(limit, 1), 100))).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long companyId) { requireCompany(companyId); return notifications.countByUserIdAndCompanyIdAndReadAtIsNull(auth.getCurrentUserId(), companyId); }

    @Transactional
    public void markRead(Long companyId, Long id) { requireCompany(companyId); if (notifications.markRead(id, auth.getCurrentUserId(), companyId) == 0) throw new ResourceNotFoundException("Notification not found."); }

    @Transactional
    public void markAllRead(Long companyId) { requireCompany(companyId); notifications.markAllRead(auth.getCurrentUserId(), companyId); }

    @Transactional
    public void create(AppUser user, Company company, NotificationType type, String title, String message, Long shiftId) {
        createNotification(user, company, type, title, message, shiftId, true);
    }

    @Transactional
    public void createWithoutEmail(AppUser user, Company company, NotificationType type, String title, String message, Long shiftId) {
        createNotification(user, company, type, title, message, shiftId, false);
    }

    private void createNotification(AppUser user, Company company, NotificationType type, String title, String message, Long shiftId, boolean sendEmail) {
        Notification notification = new Notification(); notification.setUser(user); notification.setCompany(company); notification.setType(type); notification.setTitle(title); notification.setMessage(message); notification.setRelatedShiftId(shiftId); notification.setTargetPath("/shifts");
        notifications.save(notification);
        if (sendEmail) {
            NotificationEmailOutbox email = new NotificationEmailOutbox(); email.setNotification(notification); email.setRecipient(user.getEmail()); email.setSubject(title); email.setBody(message + "\n\nOpen Invoice Platform: /shifts"); outbox.save(email);
        }
    }

    private void requireCompany(Long companyId) { if (!companyId.equals(context.getCompanyId())) throw new AccessDeniedBusinessException("The selected company does not match the request."); companies.findById(companyId).filter(c -> Boolean.TRUE.equals(c.getActive())).orElseThrow(() -> new ResourceNotFoundException("Company not found.")); }
    private NotificationResponseDTO toResponse(Notification n) { return new NotificationResponseDTO(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getTargetPath(), n.getRelatedShiftId(), n.getReadAt() != null, n.getCreatedAt()); }
}
