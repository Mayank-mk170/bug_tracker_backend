
package com.bug_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100,
            message = "Project name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Project key is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9]{1,9}$",
            message = "Project key must be 2-10 alphanumeric characters and start with a letter"
    )
    private String projectKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
