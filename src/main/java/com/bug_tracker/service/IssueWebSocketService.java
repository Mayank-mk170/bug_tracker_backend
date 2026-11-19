
package com.bug_tracker.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IssueWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public IssueWebSocketService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    // Broadcast an issue event to all subscribers of a project
    public void publishIssueUpdate(
            String projectId,
            String eventType,
            Object issueData
    ) {

        Map<String, Object> event = new LinkedHashMap<>();

        event.put("type", eventType);
        event.put("data", issueData);
        event.put("timestamp", LocalDateTime.now());

        String destination =
                "/topic/projects/" + projectId + "/issues";

        messagingTemplate.convertAndSend(destination, (Object) event);    }
}
