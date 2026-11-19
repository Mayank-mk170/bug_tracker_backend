
package com.bug_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueAssignmentRequest {

    // Developer email to assign the issue
    @NotBlank(message = "Assignee email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String assigneeEmail;
}
