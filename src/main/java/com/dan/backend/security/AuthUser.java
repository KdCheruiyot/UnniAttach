package com.dan.backend.security;

import com.dan.backend.enums.Role;

/** The logged-in user, available in controllers via @AuthenticationPrincipal. */
public record AuthUser(Long id, String email, Role role) {
}
