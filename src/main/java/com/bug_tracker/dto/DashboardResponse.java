package com.bug_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    private long totalIssues;

    private long todoIssues;

    private long inProgressIssues;

    private long doneIssues;

    private long lowPriority;

    private long mediumPriority;

    private long highPriority;

    private long criticalPriority;

    private long myAssignedIssues;
}