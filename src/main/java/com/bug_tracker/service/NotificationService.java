
package com.bug_tracker.service;

import com.bug_tracker.model.Notification;
import com.bug_tracker.model.User;
import com.bug_tracker.repository.NotificationRepository;
import com.bug_tracker.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Get currently logged-in user
    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String email = authentication.getName().toLowerCase();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    // Create notification for a user
    public Notification createNotification(
            String userId,
            String title,
            String message,
            String type,
            String issueId,
            String projectId) {

        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIssueId(issueId);
        notification.setProjectId(projectId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Get all notifications of logged-in user
    public List<Notification> getMyNotifications() {
        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications() {
        User user = getCurrentUser();

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        user.getId());
    }

    // Get unread notification count
    public long getUnreadCount() {
        User user = getCurrentUser();

        return notificationRepository
                .countByUserIdAndReadFalse(user.getId());
    }

    // Mark notification as read
    public Notification markAsRead(String notificationId) {
        User user = getCurrentUser();

        Notification notification =
                notificationRepository.findByIdAndUserId(
                        notificationId, user.getId());

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification not found");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    // Mark all notifications as read
    public void markAllAsRead() {
        User user = getCurrentUser();

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                user.getId());

        notifications.forEach(notification ->
                notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }
}
