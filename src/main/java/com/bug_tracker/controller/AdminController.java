
package com.bug_tracker.controller;

import com.bug_tracker.dto.AdminDashboardResponse;
import com.bug_tracker.dto.RoleUpdateRequest;
import com.bug_tracker.dto.UserResponse;
import com.bug_tracker.model.User;
import com.bug_tracker.repository.ProjectRepository;
import com.bug_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public AdminDashboardResponse getDashboardStats() {

        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByRoleIgnoreCase("MANAGER"),
                userRepository.countByRoleIgnoreCase("DEVELOPER"),
                projectRepository.count()
        );
    }

    // Get All Users
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    // Update User Role
    @PutMapping("/users/{userId}/role")
    public UserResponse updateUserRole(
            @PathVariable String userId,
            @RequestBody RoleUpdateRequest request) {

        log.info("Role update requested for user ID: {}", userId);

        if (request == null || request.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        String newRole = request.getRole()
                .trim()
                .toUpperCase(Locale.ROOT);

        log.info("Requested new role: {}", newRole);

        if (!newRole.equals("MANAGER") &&
                !newRole.equals("DEVELOPER")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only MANAGER and DEVELOPER roles are allowed"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin role cannot be modified"
            );
        }

        try {
            user.setRole(newRole);

            User updatedUser = userRepository.save(user);

            log.info(
                    "User role successfully updated. User: {}, New role: {}",
                    updatedUser.getEmail(),
                    updatedUser.getRole()
            );

            return convertToUserResponse(updatedUser);

        } catch (Exception exception) {

            log.error(
                    "Failed to update role for user ID: {}",
                    userId,
                    exception
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update user role"
            );
        }
    }

    // Convert User Entity to UserResponse DTO
    private UserResponse convertToUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
