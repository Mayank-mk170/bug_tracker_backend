
package com.bug_tracker.repository;

import com.bug_tracker.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository
        extends MongoRepository<Notification, String> {

    // Get all notifications of a user
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Get unread notifications
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);

    // Count unread notifications
    long countByUserIdAndReadFalse(String userId);

    // Find a notification belonging to a user
    Notification findByIdAndUserId(String id, String userId);
}
