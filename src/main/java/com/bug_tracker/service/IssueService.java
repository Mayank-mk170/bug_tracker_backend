package com.bug_tracker.service;

import com.bug_tracker.dto.IssueAssignmentRequest;
import com.bug_tracker.dto.IssueRequest;
import com.bug_tracker.dto.IssueResponse;
import com.bug_tracker.dto.IssueUpdateRequest;

import com.bug_tracker.model.Comment;
import com.bug_tracker.model.Issue;
import com.bug_tracker.model.Project;
import com.bug_tracker.model.ProjectMember;
import com.bug_tracker.model.User;

import com.bug_tracker.repository.CommentRepository;
import com.bug_tracker.repository.IssueRepository;
import com.bug_tracker.repository.ProjectMemberRepository;
import com.bug_tracker.repository.ProjectRepository;
import com.bug_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IssueService {

    private static final List<String> VALID_STATUSES =
            List.of("TODO", "IN_PROGRESS", "DONE");

    private static final List<String> VALID_PRIORITIES =
            List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("createdAt", "updatedAt", "title", "priority", "status");

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final IssueWebSocketService issueWebSocketService;


    // CREATE ISSUE
    public IssueResponse createIssue(IssueRequest request) {

        Authentication authentication = currentAuthentication();

        User reporter = findUser(authentication.getName());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        boolean admin = isAdmin(authentication);

        ProjectMember reporterMembership =
                findMembershipOrNull(project.getId(), reporter.getId());

        if (!admin && reporterMembership == null) {
            throw new AccessDeniedException(
                    "You are not a member of this project");
        }

        String priority = normalizeOrDefault(
                request.getPriority(), "MEDIUM");

        validatePriority(priority);

        Issue issue = new Issue();

        issue.setTitle(request.getTitle().trim());
        issue.setDescription(request.getDescription().trim());

        issue.setProjectId(project.getId());
        issue.setProjectKey(project.getProjectKey());

        issue.setReporterId(reporter.getId());
        issue.setReporterEmail(reporter.getEmail());

        issue.setStatus("TODO");
        issue.setPriority(priority);

        // Assign developer if email is provided
        if (request.getAssigneeEmail() != null
                && !request.getAssigneeEmail().isBlank()) {

            // Only ADMIN, OWNER and MANAGER can assign during creation
            if (!admin && (reporterMembership == null
                    || !hasRole(reporterMembership, "OWNER", "MANAGER"))) {

                throw new AccessDeniedException(
                        "Only project owners and managers can assign issues");
            }

            User assignee = findUser(request.getAssigneeEmail());

            validateDeveloperAssignee(project.getId(), assignee);

            issue.setAssigneeId(assignee.getId());
            issue.setAssigneeEmail(assignee.getEmail());
        }

        LocalDateTime now = LocalDateTime.now();

        issue.setCreatedAt(now);
        issue.setUpdatedAt(now);

        Issue savedIssue = issueRepository.save(issue);

        IssueResponse response = IssueResponse.from(savedIssue);

        issueWebSocketService.publishIssueUpdate(
                savedIssue.getProjectId(),
                "ISSUE_CREATED",
                response
        );

        return response;
    }


    // GET ISSUES BY PROJECT
    public List<IssueResponse> getIssuesByProject(String projectId) {

        validateProjectAccess(projectId);

        return issueRepository.findByProjectId(projectId)
                .stream()
                .map(IssueResponse::from)
                .toList();
    }


    // UPDATE ISSUE STATUS
    public IssueResponse updateIssueStatus(
            String issueId,
            String status) {

        Issue issue = findIssue(issueId);

        validateIssueStatusPermission(issue);

        String normalizedStatus =
                normalizeRequired(status, "Status");

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Status must be TODO, IN_PROGRESS or DONE");
        }

        issue.setStatus(normalizedStatus);
        issue.setUpdatedAt(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        IssueResponse response = IssueResponse.from(savedIssue);

        issueWebSocketService.publishIssueUpdate(
                savedIssue.getProjectId(),
                "ISSUE_STATUS_UPDATED",
                response
        );

        return response;    }


    // UPDATE ISSUE DETAILS
    public IssueResponse updateIssue(
            String issueId,
            IssueUpdateRequest request) {

        Issue issue = findIssue(issueId);

        validateIssuePermission(issue, "OWNER", "MANAGER");

        String priority =
                normalizeRequired(request.getPriority(), "Priority");

        validatePriority(priority);

        issue.setTitle(request.getTitle().trim());
        issue.setDescription(request.getDescription().trim());
        issue.setPriority(priority);
        issue.setUpdatedAt(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        IssueResponse response = IssueResponse.from(savedIssue);

        issueWebSocketService.publishIssueUpdate(
                savedIssue.getProjectId(),
                "ISSUE_UPDATED",
                response
        );

        return response;    }


    // ASSIGN ISSUE
    public IssueResponse assignIssue(
            String issueId,
            IssueAssignmentRequest request) {

        Issue issue = findIssue(issueId);

        validateIssuePermission(issue, "OWNER", "MANAGER");

        User assignee = findUser(request.getAssigneeEmail());

        validateDeveloperAssignee(issue.getProjectId(), assignee);

        issue.setAssigneeId(assignee.getId());
        issue.setAssigneeEmail(assignee.getEmail());
        issue.setUpdatedAt(LocalDateTime.now());

        Issue savedIssue = issueRepository.save(issue);

        IssueResponse response = IssueResponse.from(savedIssue);

        issueWebSocketService.publishIssueUpdate(
                savedIssue.getProjectId(),
                "ISSUE_ASSIGNED",
                response
        );

        return response;    }


    // FILTER ISSUES
    public List<IssueResponse> filterIssues(
            String projectId,
            String status,
            String priority,
            String assigneeEmail) {

        validateProjectAccess(projectId);

        status = normalizeOptional(status);
        priority = normalizeOptional(priority);
        assigneeEmail = normalizeOptionalEmail(assigneeEmail);

        if (status != null && !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid issue status");
        }

        if (priority != null && !VALID_PRIORITIES.contains(priority)) {
            throw new IllegalArgumentException("Invalid issue priority");
        }

        List<Issue> issues = issueRepository.filterIssues(
                projectId,
                status,
                priority,
                assigneeEmail
        );

        return issues.stream()
                .map(IssueResponse::from)
                .toList();
    }


    // DELETE ISSUE
    public void deleteIssue(String issueId) {

        Issue issue = findIssue(issueId);

        validateIssuePermission(issue, "OWNER", "MANAGER");

        // Delete associated comments first
        List<Comment> comments =
                commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);

        commentRepository.deleteAll(comments);

        IssueResponse deletedIssue = IssueResponse.from(issue);

        issueRepository.delete(issue);

        issueWebSocketService.publishIssueUpdate(
                issue.getProjectId(),
                "ISSUE_DELETED",
                deletedIssue
        );
    }


    // PAGINATION AND SORTING
    public Page<IssueResponse> getIssuesPaginated(
            String projectId,
            int page,
            int size,
            String sortBy,
            String direction) {

        validateProjectAccess(projectId);

        validatePagination(page, size);

        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sorting field");
        }

        String normalizedDirection = direction == null
                ? "DESC"
                : direction.trim().toUpperCase(Locale.ROOT);

        if (!normalizedDirection.equals("ASC")
                && !normalizedDirection.equals("DESC")) {

            throw new IllegalArgumentException(
                    "Direction must be ASC or DESC");
        }

        Sort.Direction sortDirection =
                Sort.Direction.fromString(normalizedDirection);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return issueRepository.findByProjectId(projectId, pageable)
                .map(IssueResponse::from);
    }


    // SEARCH ISSUES
    public Page<IssueResponse> searchIssues(
            String projectId,
            String keyword,
            int page,
            int size) {

        validateProjectAccess(projectId);

        validatePagination(page, size);

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty");
        }

        if (keyword.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Search keyword cannot exceed 100 characters");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Treat search input literally instead of as a regex expression
        String safeRegex = Pattern.quote(keyword.trim());

        return issueRepository.searchIssues(
                projectId,
                safeRegex,
                pageable
        ).map(IssueResponse::from);
    }


    // VALIDATE ISSUE STATUS PERMISSION
    private void validateIssueStatusPermission(Issue issue) {

        Authentication authentication = currentAuthentication();

        // ADMIN has full access
        if (isAdmin(authentication)) {
            return;
        }

        User user = findUser(authentication.getName());

        ProjectMember membership =
                findMembershipOrThrow(
                        issue.getProjectId(),
                        user.getId()
                );

        String role = membership.getProjectRole();

        // OWNER and MANAGER can update any issue status
        if ("OWNER".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)) {
            return;
        }

        // DEVELOPER can update only assigned issues
        if ("DEVELOPER".equalsIgnoreCase(role)
                && user.getId().equals(issue.getAssigneeId())) {
            return;
        }

        throw new AccessDeniedException(
                "You can only update the status of issues assigned to you");
    }


    // VALIDATE ISSUE EDIT / ASSIGN / DELETE PERMISSION
    private void validateIssuePermission(
            Issue issue,
            String... allowedRoles) {

        Authentication authentication = currentAuthentication();

        // ADMIN has full access
        if (isAdmin(authentication)) {
            return;
        }

        User user = findUser(authentication.getName());

        ProjectMember membership =
                findMembershipOrThrow(
                        issue.getProjectId(),
                        user.getId()
                );

        for (String allowedRole : allowedRoles) {

            if (allowedRole.equalsIgnoreCase(
                    membership.getProjectRole())) {
                return;
            }
        }

        throw new AccessDeniedException(
                "You do not have permission to perform this action");
    }


    // VALIDATE PROJECT ACCESS
    private void validateProjectAccess(String projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project not found");
        }

        Authentication authentication = currentAuthentication();

        // ADMIN can access all projects
        if (isAdmin(authentication)) {
            return;
        }

        User user = findUser(authentication.getName());

        findMembershipOrThrow(projectId, user.getId());
    }


    // VALIDATE ASSIGNEE
    private void validateDeveloperAssignee(
            String projectId,
            User assignee) {

        ProjectMember membership =
                findMembershipOrThrow(
                        projectId,
                        assignee.getId()
                );

        if (!"DEVELOPER".equalsIgnoreCase(
                membership.getProjectRole())) {

            throw new IllegalArgumentException(
                    "Only project developers can be assigned issues");
        }
    }


    // FIND MEMBERSHIP
    private ProjectMember findMembershipOrThrow(
            String projectId,
            String userId) {

        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this project"));
    }


    // FIND MEMBERSHIP OR NULL
    private ProjectMember findMembershipOrNull(
            String projectId,
            String userId) {

        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElse(null);
    }


    // FIND ISSUE
    private Issue findIssue(String issueId) {

        return issueRepository.findById(issueId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Issue not found"));
    }


    // FIND USER
    private User findUser(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "User email is required");
        }

        return userRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
                new IllegalArgumentException("User not found"));
    }


    // CURRENT AUTHENTICATION
    private Authentication currentAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return authentication;
    }


    // CHECK ADMIN ROLE
    private boolean isAdmin(Authentication authentication) {

        return authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
    }


    // CHECK PROJECT ROLE
    private boolean hasRole(
            ProjectMember membership,
            String... roles) {

        for (String role : roles) {

            if (role.equalsIgnoreCase(
                    membership.getProjectRole())) {
                return true;
            }
        }

        return false;
    }


    // NORMALIZE REQUIRED VALUE
    private String normalizeRequired(
            String value,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }


    // NORMALIZE OPTIONAL VALUE
    private String normalizeOptional(String value) {

        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }


    // NORMALIZE OPTIONAL EMAIL
    private String normalizeOptionalEmail(String value) {

        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }


    // NORMALIZE DEFAULT VALUE
    private String normalizeOrDefault(
            String value,
            String defaultValue) {

        return value == null || value.isBlank()
                ? defaultValue
                : value.trim().toUpperCase(Locale.ROOT);
    }


    // VALIDATE PRIORITY
    private void validatePriority(String priority) {

        if (!VALID_PRIORITIES.contains(priority)) {
            throw new IllegalArgumentException(
                    "Priority must be LOW, MEDIUM, HIGH or CRITICAL");
        }
    }


    // VALIDATE PAGINATION
    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }


    // DASHBOARD ACCESS
    public void validateDashboardAccess(String projectId) {

        validateProjectAccess(projectId);
    }

    public IssueWebSocketService getIssueWebSocketService() {
        return issueWebSocketService;
    }
}
