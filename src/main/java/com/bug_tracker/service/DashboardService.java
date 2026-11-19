package com.bug_tracker.service;

import com.bug_tracker.dto.DashboardResponse;
import com.bug_tracker.model.User;
import com.bug_tracker.repository.IssueRepository;
import com.bug_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueService issueService;

    public DashboardResponse getProjectDashboard(String projectId) {

        // Validate that the logged-in user can access this project
        issueService.validateDashboardAccess(projectId);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName()
                .trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return new DashboardResponse(
                issueRepository.countByProjectId(projectId),

                issueRepository.countByProjectIdAndStatus(
                        projectId, "TODO"),

                issueRepository.countByProjectIdAndStatus(
                        projectId, "IN_PROGRESS"),

                issueRepository.countByProjectIdAndStatus(
                        projectId, "DONE"),

                issueRepository.countByProjectIdAndPriority(
                        projectId, "LOW"),

                issueRepository.countByProjectIdAndPriority(
                        projectId, "MEDIUM"),

                issueRepository.countByProjectIdAndPriority(
                        projectId, "HIGH"),

                issueRepository.countByProjectIdAndPriority(
                        projectId, "CRITICAL"),

                issueRepository.countByProjectIdAndAssigneeId(
                        projectId, user.getId())
        );
    }
}