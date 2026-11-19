package com.bug_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}