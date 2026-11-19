
package com.bug_tracker.controller;

import com.bug_tracker.dto.IssueAssignmentRequest;
import com.bug_tracker.dto.IssueRequest;
import com.bug_tracker.dto.IssueResponse;
import com.bug_tracker.dto.IssueStatusRequest;
import com.bug_tracker.dto.IssueUpdateRequest;
import com.bug_tracker.service.IssueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    // Create a new issue
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @Valid @RequestBody IssueRequest request) {

        IssueResponse response = issueService.createIssue(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all issues by project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<IssueResponse>> getIssuesByProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                issueService.getIssuesByProject(projectId)
        );
    }

    // Update issue status
    @PatchMapping("/{issueId}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            @PathVariable String issueId,
            @Valid @RequestBody IssueStatusRequest request) {

        IssueResponse response = issueService.updateIssueStatus(
                issueId,
                request.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    // Update issue details
    @PutMapping("/{issueId}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable String issueId,
            @Valid @RequestBody IssueUpdateRequest request) {

        IssueResponse response = issueService.updateIssue(
                issueId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // Assign issue to a developer
    @PatchMapping("/{issueId}/assign")
    public ResponseEntity<IssueResponse> assignIssue(
            @PathVariable String issueId,
            @Valid @RequestBody IssueAssignmentRequest request) {

        IssueResponse response = issueService.assignIssue(
                issueId,
                request
        );

        return ResponseEntity.ok(response);
    }

    // Filter issues
    @GetMapping("/filter")
    public ResponseEntity<List<IssueResponse>> filterIssues(
            @RequestParam String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assigneeEmail) {

        List<IssueResponse> response = issueService.filterIssues(
                projectId,
                status,
                priority,
                assigneeEmail
        );

        return ResponseEntity.ok(response);
    }

    // Delete issue
    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable String issueId) {

        issueService.deleteIssue(issueId);

        return ResponseEntity.noContent().build();
    }

    // Get paginated issues
    @GetMapping("/project/{projectId}/paginated")
    public ResponseEntity<Page<IssueResponse>> getIssuesPaginated(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        return ResponseEntity.ok(
                issueService.getIssuesPaginated(
                        projectId,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // Search issues
    @GetMapping("/search")
    public ResponseEntity<Page<IssueResponse>> searchIssues(
            @RequestParam String projectId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                issueService.searchIssues(
                        projectId,
                        keyword,
                        page,
                        size
                )
        );
    }
}
