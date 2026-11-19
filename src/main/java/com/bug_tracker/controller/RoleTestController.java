
package com.bug_tracker.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class RoleTestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Welcome Admin! Full access granted.";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String managerAccess() {
        return "Welcome Manager! Project management access granted.";
    }

    @GetMapping("/developer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public String developerAccess() {
        return "Welcome Developer! Development access granted.";
    }
}
