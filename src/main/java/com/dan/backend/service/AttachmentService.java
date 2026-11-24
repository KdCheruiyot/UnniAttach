package com.dan.backend.service;

import com.dan.backend.dto.Dtos.AssignSupervisorRequest;
import com.dan.backend.dto.Dtos.AttachmentResponse;
import com.dan.backend.entity.Attachment;
import com.dan.backend.entity.User;
import com.dan.backend.enums.AttachmentStatus;
import com.dan.backend.enums.Role;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.AttachmentRepository;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.AuthUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachments;
    private final UserRepository users;

    public AttachmentService(AttachmentRepository attachments, UserRepository users) {
        this.attachments = attachments;
        this.users = users;
    }

    /** Each role sees only its own attachments; admins see all. */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> list(AuthUser me) {
        List<Attachment> found = switch (me.role()) {
            case STUDENT -> attachments.findByStudentIdOrderByCreatedAtDesc(me.id());
            case COMPANY -> attachments.findByCompanyIdOrderByCreatedAtDesc(me.id());
            case SUPERVISOR -> attachments.findBySupervisorIdOrderByCreatedAtDesc(me.id());
            case ADMIN -> attachments.findAllByOrderByCreatedAtDesc();
        };
        return found.stream().map(AttachmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AttachmentResponse get(Long id, AuthUser me) {
        return AttachmentResponse.from(loadAccessible(id, me));
    }

    public AttachmentResponse assignSupervisor(Long id, AssignSupervisorRequest r) {
        Attachment a = find(id);
        User supervisor = users.findById(r.supervisorId())
                .orElseThrow(() -> ApiException.notFound("Supervisor not found"));
        if (supervisor.getRole() != Role.SUPERVISOR) {
            throw ApiException.badRequest("That user is not a supervisor");
        }
        if (!supervisor.isEnabled()) {
            throw ApiException.badRequest("That supervisor account is disabled");
        }
        a.setSupervisor(supervisor);
        return AttachmentResponse.from(attachments.save(a));
    }

    public AttachmentResponse complete(Long id, AuthUser me) {
        Attachment a = find(id);
        if (me.role() != Role.ADMIN && !isCompanyOwner(a, me)) {
            throw ApiException.forbidden("Only the host company or an admin can complete this attachment");
        }
        if (a.getStatus() != AttachmentStatus.ONGOING) {
            throw ApiException.conflict("This attachment is already completed");
        }
        a.setStatus(AttachmentStatus.COMPLETED);
        a.setCompletedAt(LocalDateTime.now());
        return AttachmentResponse.from(attachments.save(a));
    }

    public Attachment find(Long id) {
        return attachments.findById(id).orElseThrow(() -> ApiException.notFound("Attachment not found"));
    }

    /** Loads the attachment, or throws 403 if the user is not part of it. */
    public Attachment loadAccessible(Long id, AuthUser me) {
        Attachment a = find(id);
        boolean allowed = me.role() == Role.ADMIN
                || isStudentOwner(a, me)
                || isCompanyOwner(a, me)
                || isAssignedSupervisor(a, me);
        if (!allowed) {
            throw ApiException.forbidden("You are not part of this attachment");
        }
        return a;
    }

    public boolean isStudentOwner(Attachment a, AuthUser me) {
        return me.role() == Role.STUDENT && a.getStudent().getId().equals(me.id());
    }

    public boolean isCompanyOwner(Attachment a, AuthUser me) {
        return me.role() == Role.COMPANY && a.getCompany().getId().equals(me.id());
    }

    public boolean isAssignedSupervisor(Attachment a, AuthUser me) {
        return me.role() == Role.SUPERVISOR && a.getSupervisor() != null
                && a.getSupervisor().getId().equals(me.id());
    }
}
