
package com.bug_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueRequest {

    // Issue title
    @NotBlank(message = "Issue title is required")
    @Size(
            min = 3,
            max = 100,
            message = "Title must be between 3 and 100 characters"
    )
    private String title;

    // Issue description
    @NotBlank(message = "Issue description is required")
    @Size(
            min = 10,
            max = 2000,
            message = "Description must be between 10 and 2000 characters"
    )
    private String description;

    // Project information
    @NotBlank(message = "Project ID is required")
    private String projectId;

    // Optional developer assignment
    @Email(message = "Please provide a valid assignee email")
    private String assigneeEmail;

    // Optional priority (defaults to MEDIUM in IssueService)
    @Pattern(
            regexp = "(?i)LOW|MEDIUM|HIGH|CRITICAL",
            message = "Priority must be LOW, MEDIUM, HIGH or CRITICAL"
    )
    private String priority;
}
