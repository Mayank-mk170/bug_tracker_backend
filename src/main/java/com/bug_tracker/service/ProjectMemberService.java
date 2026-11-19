
package com.bug_tracker.service;

import com.bug_tracker.model.Project;
import com.bug_tracker.model.ProjectMember;
import com.bug_tracker.model.User;

import com.bug_tracker.repository.ProjectRepository;
import com.bug_tracker.repository.ProjectMemberRepository;
import com.bug_tracker.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectMemberService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository) {

        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    // ADD MEMBER
    public ProjectMember addMember(
            String projectId,
            String email,
            String projectRole,
            String requesterEmail) {

        // Check project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        // Find requester
        User requester = findUser(requesterEmail);

        // Check ADMIN or project OWNER/MANAGER
        String requesterRole = getRequesterProjectRole(
                projectId, requester);

        boolean isAdmin = isAdmin(requester);
        boolean isOwner = "OWNER".equals(requesterRole);
        boolean isManager = "MANAGER".equals(requesterRole);

        if (!isAdmin && !isOwner && !isManager) {
            throw new AccessDeniedException(
                    "You cannot manage this project team");
        }

        // Validate requested project role
        if (projectRole == null || projectRole.isBlank()) {
            throw new IllegalArgumentException(
                    "Project role is required");
        }

        String normalizedRole =
                projectRole.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("MANAGER")
                && !normalizedRole.equals("DEVELOPER")) {

            throw new IllegalArgumentException(
                    "Project role must be MANAGER or DEVELOPER");
        }

        // MANAGER can add only DEVELOPER
        if (isManager && !isAdmin && !isOwner
                && !normalizedRole.equals("DEVELOPER")) {

            throw new AccessDeniedException(
                    "Managers can only add developers");
        }

        // Find new member
        User user = findUser(email);

        // Prevent duplicate membership
        if (projectMemberRepository.existsByProjectIdAndUserId(
                projectId, user.getId())) {

            throw new IllegalArgumentException(
                    "User is already a project member");
        }

        // Create membership
        ProjectMember member = new ProjectMember();

        member.setProjectId(project.getId());
        member.setUserId(user.getId());
        member.setEmail(user.getEmail());
        member.setProjectRole(normalizedRole);
        member.setJoinedAt(LocalDateTime.now());

        return projectMemberRepository.save(member);
    }

    // GET PROJECT MEMBERS
    public List<ProjectMember> getProjectMembers(
            String projectId,
            String requesterEmail) {

        // Check project exists
        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        User requester = findUser(requesterEmail);

        // ADMIN can view all project members
        if (isAdmin(requester)) {
            return projectMemberRepository.findByProjectId(projectId);
        }

        // Other users must be project members
        boolean isMember =
                projectMemberRepository.existsByProjectIdAndUserId(
                        projectId,
                        requester.getId());

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this project");
        }

        return projectMemberRepository.findByProjectId(projectId);
    }

    // REMOVE MEMBER
    public void removeMember(
            String projectId,
            String userId,
            String requesterEmail) {

        // Check project exists
        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found"));

        User requester = findUser(requesterEmail);

        // Find target member
        ProjectMember target = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project member not found"));

        String requesterRole = getRequesterProjectRole(
                projectId, requester);

        boolean isAdmin = isAdmin(requester);
        boolean isOwner = "OWNER".equals(requesterRole);
        boolean isManager = "MANAGER".equals(requesterRole);

        // Only ADMIN, OWNER and MANAGER can remove members
        if (!isAdmin && !isOwner && !isManager) {
            throw new AccessDeniedException(
                    "You cannot remove project members");
        }

        String targetRole = target.getProjectRole();

        // Owner cannot be removed
        if ("OWNER".equalsIgnoreCase(targetRole)) {
            throw new AccessDeniedException(
                    "Project owner cannot be removed");
        }

        // MANAGER can remove only DEVELOPER
        if (isManager && !isAdmin && !isOwner
                && !"DEVELOPER".equalsIgnoreCase(targetRole)) {

            throw new AccessDeniedException(
                    "Managers can only remove developers");
        }

        projectMemberRepository.delete(target);
    }

    // FIND USER BY EMAIL
    private User findUser(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required");
        }

        return userRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
                new IllegalArgumentException("User not found"));
    }

    // GET REQUESTER PROJECT ROLE
    private String getRequesterProjectRole(
            String projectId,
            User requester) {

        return projectMemberRepository
                .findByProjectIdAndUserId(
                        projectId,
                        requester.getId())
                .map(ProjectMember::getProjectRole)
                .orElse(null);
    }

    // CHECK GLOBAL ADMIN ROLE
    private boolean isAdmin(User user) {

        return user.getRole() != null
                && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
