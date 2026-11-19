
package com.bug_tracker.dto;

import com.bug_tracker.model.Issue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueResponse {

    // Issue information
    private String id;
    private String title;
    private String description;

    // Project information
    private String projectId;
    private String projectKey;

    // Reporter information
    private String reporterId;
    private String reporterEmail;

    // Assignee information
    private String assigneeId;
    private String assigneeEmail;

    // Status and priority
    private String status;
    private String priority;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Convert Issue model into IssueResponse DTO
    public static IssueResponse from(Issue issue) {

        if (issue == null) {
            throw new IllegalArgumentException("Issue cannot be null");
        }

        IssueResponse response = new IssueResponse();

        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());

        response.setProjectId(issue.getProjectId());
        response.setProjectKey(issue.getProjectKey());

        response.setReporterId(issue.getReporterId());
        response.setReporterEmail(issue.getReporterEmail());

        response.setAssigneeId(issue.getAssigneeId());
        response.setAssigneeEmail(issue.getAssigneeEmail());

        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());

        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());

        return response;
    }
}
