
package com.bug_tracker.dto;

import com.bug_tracker.model.ProjectMember;

import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private String id;
    private String userId;
    private String email;
    private String projectRole;
    private LocalDateTime joinedAt;

    public ProjectMemberResponse(ProjectMember member) {
        this.id = member.getId();
        this.userId = member.getUserId();
        this.email = member.getEmail();
        this.projectRole = member.getProjectRole();
        this.joinedAt = member.getJoinedAt();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
