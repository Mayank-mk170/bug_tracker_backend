
package com.bug_tracker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    private String issueId;

    private String userId;
    private String userEmail;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
