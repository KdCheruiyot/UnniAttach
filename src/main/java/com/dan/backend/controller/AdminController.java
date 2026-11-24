package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.AdminCreateUserRequest;
import com.dan.backend.dto.Dtos.EnabledRequest;
import com.dan.backend.dto.Dtos.StatsResponse;
import com.dan.backend.dto.UserDto;
import com.dan.backend.enums.Role;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** All users, or only one role with ?role=SUPERVISOR. */
    @GetMapping("/users")
    public List<UserDto> users(@RequestParam(required = false) Role role) {
        return adminService.listUsers(role);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return adminService.createUser(request);
    }

    @PatchMapping("/users/{id}/enabled")
    public UserDto setEnabled(@PathVariable Long id, @AuthenticationPrincipal AuthUser me,
                              @Valid @RequestBody EnabledRequest request) {
        return adminService.setEnabled(id, request.enabled(), me);
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.stats();
    }
}
