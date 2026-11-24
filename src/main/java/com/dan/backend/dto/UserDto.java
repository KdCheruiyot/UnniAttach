package com.dan.backend.dto;

import com.dan.backend.entity.User;
import com.dan.backend.enums.Role;
import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean enabled,
        String phone,
        String organization,
        String registrationNumber,
        String course,
        LocalDateTime createdAt) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole(),
                u.isEnabled(), u.getPhone(), u.getOrganization(), u.getRegistrationNumber(),
                u.getCourse(), u.getCreatedAt());
    }
}
