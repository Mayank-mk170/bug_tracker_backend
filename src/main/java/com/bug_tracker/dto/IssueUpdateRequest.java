
package com.bug_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueUpdateRequest {

    // Updated issue title
    @NotBlank(message = "Title is required")
    @Size(
            min = 3,
            max = 100,
            message = "Title must be between 3 and 100 characters"
    )
    private String title;

    // Updated issue description
    @NotBlank(message = "Description is required")
    @Size(
            min = 10,
            max = 2000,
            message = "Description must be between 10 and 2000 characters"
    )
    private String description;

    // Updated issue priority
    @NotBlank(message = "Priority is required")
    @Pattern(
            regexp = "(?i)LOW|MEDIUM|HIGH|CRITICAL",
            message = "Priority must be LOW, MEDIUM, HIGH or CRITICAL"
    )
    private String priority;
}
