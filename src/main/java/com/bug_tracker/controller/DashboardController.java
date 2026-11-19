package com.bug_tracker.controller;

import com.bug_tracker.dto.DashboardResponse;
import com.bug_tracker.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<DashboardResponse> getProjectDashboard(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                dashboardService.getProjectDashboard(projectId)
        );
    }
}