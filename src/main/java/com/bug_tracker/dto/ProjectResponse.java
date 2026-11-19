
package com.bug_tracker.dto;

import com.bug_tracker.model.Project;

import java.time.LocalDateTime;

public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String projectKey;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.projectKey = project.getProjectKey();
        this.createdBy = project.getCreatedBy();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
