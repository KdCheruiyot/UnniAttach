package com.dan.backend.dto;

import com.dan.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") String password,
        @NotNull(message = "must be STUDENT or COMPANY") Role role,
        @Size(max = 30) String phone,
        @Size(max = 150) String organization,
        @Size(max = 60) String registrationNumber,
        @Size(max = 120) String course) {
}
