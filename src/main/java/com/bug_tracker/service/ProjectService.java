
package com.bug_tracker.service;

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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final CommentRepository commentRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            IssueRepository issueRepository,
            CommentRepository commentRepository) {

        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
        this.commentRepository = commentRepository;
    }

    // CREATE PROJECT
    public Project createProject(
            String name,
            String description,
            String projectKey,
            String createdBy) {

        String normalizedKey =
                projectKey.trim().toUpperCase(Locale.ROOT);

        if (projectRepository.existsByProjectKey(normalizedKey)) {
            throw new IllegalArgumentException(
                    "Project key already exists");
        }

        User creator = userRepository.findByEmail(
                normalizeEmail(createdBy)
        ).orElseThrow(() ->
                new IllegalArgumentException("Project creator not found"));

        Project project = new Project();

        project.setName(name);
        project.setDescription(description);
        project.setProjectKey(normalizedKey);
        project.setCreatedBy(creator.getEmail());

        LocalDateTime now = LocalDateTime.now();

        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        Project savedProject = projectRepository.save(project);

        // Automatically add creator as OWNER
        ProjectMember owner = new ProjectMember();

        owner.setProjectId(savedProject.getId());
        owner.setUserId(creator.getId());
        owner.setEmail(creator.getEmail());
        owner.setProjectRole("OWNER");
        owner.setJoinedAt(now);

        projectMemberRepository.save(owner);

        return savedProject;
    }

    // GET MY PROJECTS
    public List<Project> getMyProjects(String email) {

        String normalizedEmail = normalizeEmail(email);

        // ADMIN can view all projects
        if (isAdmin()) {
            return projectRepository.findAll();
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Project> projects = new ArrayList<>();
        Set<String> addedProjectIds = new HashSet<>();

        // Include projects created by this user
        List<Project> createdProjects =
                projectRepository.findByCreatedBy(normalizedEmail);

        for (Project project : createdProjects) {
            projects.add(project);
            addedProjectIds.add(project.getId());
        }

        // Include projects where this user is a member
        List<ProjectMember> memberships =
                projectMemberRepository.findByUserId(user.getId());

        for (ProjectMember membership : memberships) {

            String projectId = membership.getProjectId();

            if (addedProjectIds.add(projectId)) {

                projectRepository.findById(projectId)
                        .ifPresent(projects::add);
            }
        }

        return projects;
    }

    // GET PROJECT BY ID
    public Project getProjectById(String id, String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        checkProjectAccess(project, email);

        return project;
    }

    // UPDATE PROJECT
    public Project updateProject(
            String id,
            String name,
            String description,
            String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        checkManagePermission(project, email);

        project.setName(name);
        project.setDescription(description);
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    // DELETE PROJECT WITH CASCADE CLEANUP
    public void deleteProject(String id, String email) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        checkDeletePermission(project, email);

        // 1. Find all project issues
        List<Issue> issues =
                issueRepository.findByProjectId(id);

        // 2. Delete comments belonging to those issues
        for (Issue issue : issues) {

            List<Comment> comments =
                    commentRepository
                            .findByIssueIdOrderByCreatedAtAsc(issue.getId());

            commentRepository.deleteAll(comments);
        }

        // 3. Delete all project issues
        issueRepository.deleteAll(issues);

        // 4. Delete all project memberships
        List<ProjectMember> members =
                projectMemberRepository.findByProjectId(id);

        projectMemberRepository.deleteAll(members);

        // 5. Finally delete the project
        projectRepository.delete(project);
    }

    // CHECK PROJECT ACCESS
    private void checkProjectAccess(Project project, String email) {

        if (isAdmin()) {
            return;
        }

        String normalizedEmail = normalizeEmail(email);

        // Project creator can access their project
        if (project.getCreatedBy() != null &&
                project.getCreatedBy().equalsIgnoreCase(normalizedEmail)) {
            return;
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        boolean isMember =
                projectMemberRepository.existsByProjectIdAndUserId(
                        project.getId(),
                        user.getId()
                );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not authorized to access this project");
        }
    }

    // CHECK OWNER OR MANAGER PERMISSION FOR UPDATE
    private void checkManagePermission(Project project, String email) {

        if (isAdmin()) {
            return;
        }

        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        ProjectMember membership =
                projectMemberRepository.findByProjectIdAndUserId(
                        project.getId(),
                        user.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this project"));

        String role = membership.getProjectRole();

        if (!"OWNER".equalsIgnoreCase(role) &&
                !"MANAGER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "Only project owners and managers can update projects");
        }
    }

    // CHECK ADMIN OR PROJECT OWNER PERMISSION FOR DELETE
    private void checkDeletePermission(
            Project project,
            String email) {

        if (isAdmin()) {
            return;
        }

        User user = userRepository.findByEmail(
                normalizeEmail(email)
        ).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        ProjectMember membership =
                projectMemberRepository.findByProjectIdAndUserId(
                        project.getId(),
                        user.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this project"));

        if (!"OWNER".equalsIgnoreCase(membership.getProjectRole())) {
            throw new AccessDeniedException(
                    "Only project owners and admins can delete projects");
        }
    }

    // CHECK ADMIN ROLE
    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }

    // NORMALIZE EMAIL
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
