
package com.bug_tracker.dto;

public class LoginResponse {

    private String message;
    private String fullName;
    private String email;
    private String role;
    private String token;

    public LoginResponse(
            String message,
            String fullName,
            String email,
            String role,
            String token) {

        this.message = message;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
