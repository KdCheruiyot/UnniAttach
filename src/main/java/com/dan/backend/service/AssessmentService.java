package com.dan.backend.service;

import com.dan.backend.dto.Dtos.AssessmentRequest;
import com.dan.backend.dto.Dtos.AssessmentResponse;
import com.dan.backend.entity.Assessment;
import com.dan.backend.entity.Attachment;
import com.dan.backend.entity.User;
import com.dan.backend.enums.AssessmentType;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.AssessmentRepository;
import com.dan.backend.repository.UserRepository;
import com.dan.backend.security.AuthUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessments;
    private final UserRepository users;
    private final AttachmentService attachmentService;

    public AssessmentService(AssessmentRepository assessments, UserRepository users,
                             AttachmentService attachmentService) {
        this.assessments = assessments;
        this.users = users;
        this.attachmentService = attachmentService;
    }

    /** The host company gives a COMPANY assessment, the assigned supervisor a SUPERVISOR one. */
    public AssessmentResponse create(Long attachmentId, AuthUser me, AssessmentRequest r) {
        Attachment a = attachmentService.find(attachmentId);
        AssessmentType type;
        if (attachmentService.isCompanyOwner(a, me)) {
            type = AssessmentType.COMPANY;
        } else if (attachmentService.isAssignedSupervisor(a, me)) {
            type = AssessmentType.SUPERVISOR;
        } else {
            throw ApiException.forbidden("Only the host company or the assigned supervisor can assess this student");
        }
        if (assessments.existsByAttachmentIdAndType(a.getId(), type)) {
            throw ApiException.conflict("You have already submitted an assessment for this attachment");
        }
        User assessor = users.findById(me.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        Assessment as = new Assessment();
        as.setAttachment(a);
        as.setAssessor(assessor);
        as.setType(type);
        as.setScore(r.score());
        as.setComments(r.comments().trim());
        return AssessmentResponse.from(assessments.save(as));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> list(Long attachmentId, AuthUser me) {
        Attachment a = attachmentService.loadAccessible(attachmentId, me);
        return assessments.findByAttachmentIdOrderByCreatedAtAsc(a.getId())
                .stream().map(AssessmentResponse::from).toList();
    }
}
