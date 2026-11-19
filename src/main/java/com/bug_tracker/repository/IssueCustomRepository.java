
package com.bug_tracker.repository;

import com.bug_tracker.model.Issue;

import java.util.List;

public interface IssueCustomRepository {

    List<Issue> filterIssues(
            String projectId,
            String status,
            String priority,
            String assigneeEmail
    );
}
