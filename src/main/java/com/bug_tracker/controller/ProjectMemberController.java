
package com.bug_tracker.controller;

import com.bug_tracker.dto.ProjectMemberRequest;
import com.bug_tracker.dto.ProjectMemberResponse;
import com.bug_tracker.model.ProjectMember;
import com.bug_tracker.service.ProjectMemberService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(
            ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    // Add Project Member
    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectMemberRequest request,
            Authentication authentication) {

        ProjectMember member = projectMemberService.addMember(
                projectId,
                request.getEmail(),
                request.getProjectRole(),
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProjectMemberResponse(member));
    }

    // Get Project Team Members
    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @PathVariable String projectId,
            Authentication authentication) {

        List<ProjectMemberResponse> members =
                projectMemberService.getProjectMembers(
                                projectId,
                                authentication.getName()
                        )
                        .stream()
                        .map(ProjectMemberResponse::new)
                        .toList();

        return ResponseEntity.ok(members);
    }

    // Remove Project Member
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> removeMember(
            @PathVariable String projectId,
            @PathVariable String userId,
            Authentication authentication) {

        projectMemberService.removeMember(
                projectId,
                userId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Project member removed successfully"
        );
    }
}
