
package com.bug_tracker.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "project_members")
public class ProjectMember {

    @Id
    private String id;

    private String projectId;

    private String userId;

    private String email;

    private String projectRole;

    private LocalDateTime joinedAt;
}
