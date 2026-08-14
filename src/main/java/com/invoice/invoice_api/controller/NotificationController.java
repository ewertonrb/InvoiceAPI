package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.notification.NotificationResponseDTO;
import com.invoice.invoice_api.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/notifications")
public class NotificationController {

    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> list(@PathVariable Long companyId, @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(notifications.list(companyId, limit)); }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long companyId) {
        return ResponseEntity.ok(notifications.unreadCount(companyId)); }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long companyId, @PathVariable Long id) {
        notifications.markRead(companyId, id); return ResponseEntity.noContent().build(); }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long companyId) {
        notifications.markAllRead(companyId); return ResponseEntity.noContent().build(); }
}
