package com.dan.backend.service;

import com.dan.backend.dto.AuthResponse;
import com.dan.backend.dto.LoginRequest;
import com.dan.backend.dto.RegisterRequest;
import com.dan.backend.dto.UserDto;
import com.dan.backend.entity.User;
import com.dan.backend.enums.Role;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        if (r.role() != Role.STUDENT && r.role() != Role.COMPANY) {
            throw ApiException.badRequest(
                    "Only STUDENT and COMPANY accounts can self-register. Supervisor accounts are created by an admin.");
        }
        if (r.role() == Role.COMPANY && (r.organization() == null || r.organization().isBlank())) {
            throw ApiException.badRequest("Company name (organization) is required for company accounts");
        }
        User user = createUser(r.firstName(), r.lastName(), r.email(), r.password(), r.role(),
                r.phone(), r.organization(), r.registrationNumber(), r.course());
        return tokenResponse(user);
    }

    public AuthResponse login(LoginRequest r) {
        User user = users.findByEmail(r.email().trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!encoder.matches(r.password(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw ApiException.forbidden("This account has been disabled");
        }
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return UserDto.from(users.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found")));
    }

    /** Shared by self-registration and by the admin "create user" endpoint. */
    public User createUser(String firstName, String lastName, String email, String password, Role role,
                           String phone, String organization, String registrationNumber, String course) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        User u = new User();
        u.setFirstName(firstName.trim());
        u.setLastName(lastName.trim());
        u.setEmail(normalized);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setPhone(blankToNull(phone));
        u.setOrganization(blankToNull(organization));
        if (role == Role.STUDENT) {
            u.setRegistrationNumber(blankToNull(registrationNumber));
            u.setCourse(blankToNull(course));
        }
        return users.save(u);
    }

    private AuthResponse tokenResponse(User user) {
        return new AuthResponse(jwt.generateToken(user), "Bearer", jwt.getExpiresInSeconds(), UserDto.from(user));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
