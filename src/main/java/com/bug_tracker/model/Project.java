
package com.bug_tracker.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String name;

    private String description;

    private String projectKey;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
