package com.dan.backend.service;

import com.dan.backend.dto.Dtos.AdminCreateUserRequest;
import com.dan.backend.dto.Dtos.StatsResponse;
import com.dan.backend.dto.UserDto;
import com.dan.backend.entity.User;
import com.dan.backend.enums.AttachmentStatus;
import com.dan.backend.enums.PlacementStatus;
import com.dan.backend.enums.Role;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.ApplicationRepository;
import com.dan.backend.repository.AttachmentRepository;
import com.dan.backend.repository.PlacementRepository;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.AuthUser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final UserRepository users;
    private final PlacementRepository placements;
    private final ApplicationRepository applications;
    private final AttachmentRepository attachments;
    private final AuthService authService;

    public AdminService(UserRepository users, PlacementRepository placements,
                        ApplicationRepository applications, AttachmentRepository attachments,
                        AuthService authService) {
        this.users = users;
        this.placements = placements;
        this.applications = applications;
        this.attachments = attachments;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> listUsers(Role role) {
        List<User> found = role == null
                ? users.findAllByOrderByCreatedAtDesc()
                : users.findByRoleOrderByCreatedAtDesc(role);
        return found.stream().map(UserDto::from).toList();
    }

    /** Admins create SUPERVISOR accounts here (and, if needed, other admins). */
    public UserDto createUser(AdminCreateUserRequest r) {
        User u = authService.createUser(r.firstName(), r.lastName(), r.email(), r.password(), r.role(),
                r.phone(), r.organization(), null, null);
        return UserDto.from(u);
    }

    public UserDto setEnabled(Long id, boolean enabled, AuthUser me) {
        if (id.equals(me.id()) && !enabled) {
            throw ApiException.badRequest("You cannot disable your own account");
        }
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        u.setEnabled(enabled);
        return UserDto.from(users.save(u));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Map<String, Long> byRole = new LinkedHashMap<>();
        for (Role r : Role.values()) {
            byRole.put(r.name(), users.countByRole(r));
        }
        return new StatsResponse(byRole, placements.count(), placements.countByStatus(PlacementStatus.OPEN),
                applications.count(), attachments.countByStatus(AttachmentStatus.ONGOING),
                attachments.countByStatus(AttachmentStatus.COMPLETED));
    }
}
