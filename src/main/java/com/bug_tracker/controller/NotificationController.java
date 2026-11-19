
package com.bug_tracker.controller;

import com.bug_tracker.model.Notification;
import com.bug_tracker.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications() {
        return ResponseEntity.ok(
                notificationService.getMyNotifications());
    }

    // Get unread notifications
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        return ResponseEntity.ok(
                notificationService.getUnreadNotifications());
    }

    // Get unread notification count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(
                Map.of("unreadCount",
                        notificationService.getUnreadCount()));
    }

    // Mark one notification as read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable String notificationId) {
        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId));
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                Map.of("message",
                        "All notifications marked as read"));
    }
}
