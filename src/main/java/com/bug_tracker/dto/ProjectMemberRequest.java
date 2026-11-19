
package com.bug_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProjectMemberRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Project role is required")
    @Pattern(
            regexp = "MANAGER|DEVELOPER",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Role must be MANAGER or DEVELOPER"
    )
    private String projectRole;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(String projectRole) {
        this.projectRole = projectRole;
    }
}
