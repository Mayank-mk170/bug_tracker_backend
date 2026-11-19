
package com.bug_tracker.service;

import com.bug_tracker.model.User;
import com.bug_tracker.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(
            String fullName,
            String email,
            String password) {

        // Check duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email already registered");
        }

        // Create new user
        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(password));

        user.setRole("PENDING");
        user.setCreatedAt(LocalDateTime.now());

        // Save user in MongoDB
        return userRepository.save(user);
    }

    //password change
    public void changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        User user = userRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(
                currentPassword, user.getPassword())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        if (passwordEncoder.matches(
                newPassword, user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
