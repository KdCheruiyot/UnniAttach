package com.dan.backend.service;

import com.dan.backend.dto.Dtos.PlacementRequest;
import com.dan.backend.dto.Dtos.PlacementResponse;
import com.dan.backend.entity.Placement;
import com.dan.backend.entity.User;
import com.dan.backend.enums.ApplicationStatus;
import com.dan.backend.enums.PlacementStatus;
import com.dan.backend.enums.Role;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.ApplicationRepository;
import com.dan.backend.repository.PlacementRepository;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.AuthUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlacementService {

    private final PlacementRepository placements;
    private final UserRepository users;
    private final ApplicationRepository applications;

    public PlacementService(PlacementRepository placements, UserRepository users,
                            ApplicationRepository applications) {
        this.placements = placements;
        this.users = users;
        this.applications = applications;
    }

    public PlacementResponse create(AuthUser me, PlacementRequest r) {
        validateDates(r);
        User company = users.findById(me.id())
                .orElseThrow(() -> ApiException.notFound("User not found"));
        Placement p = new Placement();
        p.setCompany(company);
        copy(p, r);
        p.setStatus(PlacementStatus.OPEN);
        return toResponse(placements.save(p));
    }

    public PlacementResponse update(Long id, AuthUser me, PlacementRequest r) {
        Placement p = find(id);
        ensureOwnerOrAdmin(p, me);
        validateDates(r);
        long accepted = applications.countByPlacementIdAndStatus(p.getId(), ApplicationStatus.ACCEPTED);
        if (r.slots() < accepted) {
            throw ApiException.conflict("Slots cannot be lower than the number of accepted students (" + accepted + ")");
        }
        copy(p, r);
        return toResponse(placements.save(p));
    }

    public PlacementResponse close(Long id, AuthUser me) {
        Placement p = find(id);
        ensureOwnerOrAdmin(p, me);
        p.setStatus(PlacementStatus.CLOSED);
        return toResponse(placements.save(p));
    }

    @Transactional(readOnly = true)
    public List<PlacementResponse> listOpen(String q) {
        List<Placement> found = (q == null || q.isBlank())
                ? placements.findByStatusOrderByCreatedAtDesc(PlacementStatus.OPEN)
                : placements.search(PlacementStatus.OPEN, q.trim());
        return found.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PlacementResponse> mine(AuthUser me) {
        return placements.findByCompanyIdOrderByCreatedAtDesc(me.id()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlacementResponse get(Long id) {
        return toResponse(find(id));
    }

    public Placement find(Long id) {
        return placements.findById(id).orElseThrow(() -> ApiException.notFound("Placement not found"));
    }

    public void ensureOwnerOrAdmin(Placement p, AuthUser me) {
        boolean owner = p.getCompany().getId().equals(me.id());
        if (!owner && me.role() != Role.ADMIN) {
            throw ApiException.forbidden("This placement belongs to another company");
        }
    }

    private void validateDates(PlacementRequest r) {
        if (r.endDate().isBefore(r.startDate())) {
            throw ApiException.badRequest("endDate must be on or after startDate");
        }
    }

    private void copy(Placement p, PlacementRequest r) {
        p.setTitle(r.title().trim());
        p.setDescription(r.description().trim());
        p.setLocation(r.location().trim());
        p.setRequiredSkills(r.requiredSkills() == null || r.requiredSkills().isBlank() ? null : r.requiredSkills().trim());
        p.setSlots(r.slots());
        p.setDeadline(r.deadline());
        p.setStartDate(r.startDate());
        p.setEndDate(r.endDate());
    }

    private PlacementResponse toResponse(Placement p) {
        long accepted = applications.countByPlacementIdAndStatus(p.getId(), ApplicationStatus.ACCEPTED);
        return PlacementResponse.from(p, accepted);
    }
}
