package com.dan.backend.dto;

public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserDto user) {
}
