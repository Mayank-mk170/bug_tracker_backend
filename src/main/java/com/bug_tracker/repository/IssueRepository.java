
package com.bug_tracker.repository;

import com.bug_tracker.model.Issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IssueRepository extends MongoRepository<Issue, String>, IssueCustomRepository{

    // Find issues
    List<Issue> findByProjectId(String projectId);

    List<Issue> findByAssigneeId(String assigneeId);

    List<Issue> findByReporterId(String reporterId);


    // Filter by project and status
    List<Issue> findByProjectIdAndStatus(
            String projectId,
            String status
    );


    // Filter by project and priority
    List<Issue> findByProjectIdAndPriority(
            String projectId,
            String priority
    );


    // Filter by project and assignee
    List<Issue> findByProjectIdAndAssigneeEmail(
            String projectId,
            String assigneeEmail
    );


    // Filter by project, status and priority
    List<Issue> findByProjectIdAndStatusAndPriority(
            String projectId,
            String status,
            String priority
    );


    // Filter by project, status, priority and assignee
    List<Issue> findByProjectIdAndStatusAndPriorityAndAssigneeEmail(
            String projectId,
            String status,
            String priority,
            String assigneeEmail
    );


    // Filter by project, status and assignee
    List<Issue> findByProjectIdAndStatusAndAssigneeEmail(
            String projectId,
            String status,
            String assigneeEmail
    );


    // Filter by project, priority and assignee
    List<Issue> findByProjectIdAndPriorityAndAssigneeEmail(
            String projectId,
            String priority,
            String assigneeEmail
    );


    // Pagination
    Page<Issue> findByProjectId(
            String projectId,
            Pageable pageable
    );

    Page<Issue> findByProjectIdAndStatus(
            String projectId,
            String status,
            Pageable pageable
    );

    Page<Issue> findByProjectIdAndPriority(
            String projectId,
            String priority,
            Pageable pageable
    );

    Page<Issue> findByProjectIdAndAssigneeEmail(
            String projectId,
            String assigneeEmail,
            Pageable pageable
    );


    // Search issues by title, description or project key
    @Query("{ 'projectId': ?0, '$or': [ " +
            "{ 'title': { '$regex': ?1, '$options': 'i' } }, " +
            "{ 'description': { '$regex': ?1, '$options': 'i' } }, " +
            "{ 'projectKey': { '$regex': ?1, '$options': 'i' } } " +
            "] }")
    Page<Issue> searchIssues(
            String projectId,
            String keyword,
            Pageable pageable
    );


    // Dashboard counts
    long countByProjectId(String projectId);

    long countByProjectIdAndStatus(
            String projectId,
            String status
    );

    long countByProjectIdAndPriority(
            String projectId,
            String priority
    );

    long countByProjectIdAndAssigneeId(
            String projectId,
            String assigneeId
    );
}
