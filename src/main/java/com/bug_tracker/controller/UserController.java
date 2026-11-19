
package com.bug_tracker.controller;

import com.bug_tracker.dto.RegisterRequest;
import com.bug_tracker.dto.RegisterResponse;
import com.bug_tracker.dto.LoginRequest;
import com.bug_tracker.dto.LoginResponse;

import com.bug_tracker.model.User;
import com.bug_tracker.service.UserService;
import com.bug_tracker.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bug_tracker.dto.ChangePasswordRequest;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(
            UserService userService,
            AuthService authService) {

        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "User registered successfully"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                "Welcome " + authentication.getName()
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(
                authentication.getName(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }
}
