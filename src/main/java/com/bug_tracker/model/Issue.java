
package com.bug_tracker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "issues")
@CompoundIndexes({
        @CompoundIndex(
                name = "project_created_idx",
                def = "{'projectId': 1, 'createdAt': -1}"
        ),
        @CompoundIndex(
                name = "project_status_created_idx",
                def = "{'projectId': 1, 'status': 1, 'createdAt': -1}"
        ),
        @CompoundIndex(
                name = "project_priority_created_idx",
                def = "{'projectId': 1, 'priority': 1, 'createdAt': -1}"
        ),
        @CompoundIndex(
                name = "project_assignee_created_idx",
                def = "{'projectId': 1, 'assigneeEmail': 1, 'createdAt': -1}"
        )
})
public class Issue {

    @Id
    private String id;

    // Issue details
    private String title;
    private String description;

    // Project information
    @Indexed
    private String projectId;

    private String projectKey;

    // Reporter information
    private String reporterId;
    private String reporterEmail;

    // Assignee information
    @Indexed
    private String assigneeId;

    private String assigneeEmail;

    // Issue status and priority
    private String status;
    private String priority;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
