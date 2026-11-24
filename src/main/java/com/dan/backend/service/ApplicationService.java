package com.dan.backend.service;

import com.dan.backend.dto.Dtos.ApplicationRequest;
import com.dan.backend.dto.Dtos.ApplicationResponse;
import com.dan.backend.dto.Dtos.DecisionRequest;
import com.dan.backend.entity.Application;
import com.dan.backend.entity.Attachment;
import com.dan.backend.entity.Placement;
import com.dan.backend.entity.User;
import com.dan.backend.enums.ApplicationStatus;
import com.dan.backend.enums.AttachmentStatus;
import com.dan.backend.enums.PlacementStatus;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.ApplicationRepository;
import com.dan.backend.repository.AttachmentRepository;
import com.dan.backend.repository.PlacementRepository;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.AuthUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applications;
    private final PlacementRepository placements;
    private final AttachmentRepository attachments;
    private final UserRepository users;
    private final PlacementService placementService;

    public ApplicationService(ApplicationRepository applications, PlacementRepository placements,
                              AttachmentRepository attachments, UserRepository users,
                              PlacementService placementService) {
        this.applications = applications;
        this.placements = placements;
        this.attachments = attachments;
        this.users = users;
        this.placementService = placementService;
    }

    public ApplicationResponse apply(AuthUser me, ApplicationRequest r) {
        Placement p = placements.findById(r.placementId())
                .orElseThrow(() -> ApiException.notFound("Placement not found"));
        if (p.getStatus() != PlacementStatus.OPEN || p.getDeadline().isBefore(LocalDate.now())) {
            throw ApiException.conflict("This placement is no longer accepting applications");
        }
        if (applications.existsByStudentIdAndPlacementId(me.id(), p.getId())) {
            throw ApiException.conflict("You have already applied to this placement");
        }
        if (attachments.existsByStudentIdAndStatus(me.id(), AttachmentStatus.ONGOING)) {
            throw ApiException.conflict("You already have an ongoing attachment");
        }
        User student = users.findById(me.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        Application a = new Application();
        a.setStudent(student);
        a.setPlacement(p);
        a.setCoverLetter(r.coverLetter() == null || r.coverLetter().isBlank() ? null : r.coverLetter().trim());
        return ApplicationResponse.from(applications.save(a));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> mine(AuthUser me) {
        return applications.findByStudentIdOrderByAppliedAtDesc(me.id())
                .stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> forPlacement(Long placementId, AuthUser me) {
        Placement p = placementService.find(placementId);
        placementService.ensureOwnerOrAdmin(p, me);
        return applications.findByPlacementIdOrderByAppliedAtDesc(placementId)
                .stream().map(ApplicationResponse::from).toList();
    }

    public ApplicationResponse withdraw(Long id, AuthUser me) {
        Application a = find(id);
        if (!a.getStudent().getId().equals(me.id())) {
            throw ApiException.forbidden("This is not your application");
        }
        if (a.getStatus() != ApplicationStatus.PENDING) {
            throw ApiException.conflict("Only pending applications can be withdrawn");
        }
        a.setStatus(ApplicationStatus.WITHDRAWN);
        a.setDecidedAt(LocalDateTime.now());
        return ApplicationResponse.from(applications.save(a));
    }

    /** Company accepts or rejects. Accepting creates the Attachment record. */
    public ApplicationResponse decide(Long id, AuthUser me, DecisionRequest r) {
        Application a = find(id);
        Placement p = a.getPlacement();
        if (!p.getCompany().getId().equals(me.id())) {
            throw ApiException.forbidden("This application is for another company's placement");
        }
        if (r.status() != ApplicationStatus.ACCEPTED && r.status() != ApplicationStatus.REJECTED) {
            throw ApiException.badRequest("status must be ACCEPTED or REJECTED");
        }
        if (a.getStatus() != ApplicationStatus.PENDING) {
            throw ApiException.conflict("This application is already " + a.getStatus().name().toLowerCase());
        }

        LocalDateTime now = LocalDateTime.now();
        if (r.status() == ApplicationStatus.ACCEPTED) {
            long accepted = applications.countByPlacementIdAndStatus(p.getId(), ApplicationStatus.ACCEPTED);
            if (accepted >= p.getSlots()) {
                throw ApiException.conflict("All slots for this placement are already filled");
            }
            User student = a.getStudent();
            if (attachments.existsByStudentIdAndStatus(student.getId(), AttachmentStatus.ONGOING)) {
                throw ApiException.conflict("This student already has an ongoing attachment");
            }

            Attachment att = new Attachment();
            att.setApplication(a);
            att.setStudent(student);
            att.setCompany(p.getCompany());
            att.setPlacement(p);
            att.setStartDate(p.getStartDate());
            att.setEndDate(p.getEndDate());
            attachments.save(att);

            // The student is placed, so their other pending applications are withdrawn.
            for (Application other : applications.findByStudentIdAndStatus(student.getId(), ApplicationStatus.PENDING)) {
                if (!other.getId().equals(a.getId())) {
                    other.setStatus(ApplicationStatus.WITHDRAWN);
                    other.setRemarks("Student was accepted for another placement");
                    other.setDecidedAt(now);
                    applications.save(other);
                }
            }

            if (accepted + 1 >= p.getSlots()) {
                p.setStatus(PlacementStatus.CLOSED);
                placements.save(p);
            }
        }

        a.setStatus(r.status());
        a.setRemarks(r.remarks() == null || r.remarks().isBlank() ? null : r.remarks().trim());
        a.setDecidedAt(now);
        return ApplicationResponse.from(applications.save(a));
    }

    private Application find(Long id) {
        return applications.findById(id).orElseThrow(() -> ApiException.notFound("Application not found"));
    }
}
