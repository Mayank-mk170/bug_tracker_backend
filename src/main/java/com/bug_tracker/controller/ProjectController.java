
package com.bug_tracker.controller;

import com.bug_tracker.dto.ProjectRequest;
import com.bug_tracker.dto.ProjectResponse;
import com.bug_tracker.model.Project;
import com.bug_tracker.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create Project
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {

        Project project = projectService.createProject(
                request.getName(),
                request.getDescription(),
                request.getProjectKey(),
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProjectResponse(project));
    }

    // Get My Projects
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            Authentication authentication) {

        List<ProjectResponse> projects = projectService
                .getMyProjects(authentication.getName())
                .stream()
                .map(ProjectResponse::new)
                .toList();

        return ResponseEntity.ok(projects);
    }

    // Get Project By ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable String id,
            Authentication authentication) {

        Project project = projectService.getProjectById(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(new ProjectResponse(project));
    }

    // Update Project
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {

        Project project = projectService.updateProject(
                id,
                request.getName(),
                request.getDescription(),
                authentication.getName()
        );

        return ResponseEntity.ok(new ProjectResponse(project));
    }

    // Delete Project
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @PathVariable String id,
            Authentication authentication) {

        projectService.deleteProject(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Project deleted successfully"
        );
    }


}
