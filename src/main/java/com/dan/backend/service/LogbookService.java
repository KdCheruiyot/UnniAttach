package com.dan.backend.service;

import com.dan.backend.dto.Dtos.LogbookRequest;
import com.dan.backend.dto.Dtos.LogbookResponse;
import com.dan.backend.dto.Dtos.LogbookUpdateRequest;
import com.dan.backend.dto.Dtos.ReviewRequest;
import com.dan.backend.entity.Attachment;
import com.dan.backend.entity.LogbookEntry;
import com.dan.backend.enums.AttachmentStatus;
import com.dan.backend.exception.ApiException;
import com.dan.backend.repository.LogbookEntryRepository;
import com.dan.backend.security.AuthUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogbookService {

    private final LogbookEntryRepository entries;
    private final AttachmentService attachmentService;

    public LogbookService(LogbookEntryRepository entries, AttachmentService attachmentService) {
        this.entries = entries;
        this.attachmentService = attachmentService;
    }

    public LogbookResponse add(Long attachmentId, AuthUser me, LogbookRequest r) {
        Attachment a = attachmentService.find(attachmentId);
        if (!attachmentService.isStudentOwner(a, me)) {
            throw ApiException.forbidden("Only the student on this attachment can write logbook entries");
        }
        if (a.getStatus() != AttachmentStatus.ONGOING) {
            throw ApiException.conflict("This attachment is already completed");
        }
        LocalDate date = r.entryDate();
        if (date.isAfter(LocalDate.now())) {
            throw ApiException.badRequest("Entry date cannot be in the future");
        }
        if (date.isBefore(a.getStartDate()) || date.isAfter(a.getEndDate())) {
            throw ApiException.badRequest("Entry date must be within the attachment period ("
                    + a.getStartDate() + " to " + a.getEndDate() + ")");
        }
        if (entries.existsByAttachmentIdAndEntryDate(a.getId(), date)) {
            throw ApiException.conflict("You already have an entry for " + date);
        }
        LogbookEntry e = new LogbookEntry();
        e.setAttachment(a);
        e.setEntryDate(date);
        e.setActivities(r.activities().trim());
        e.setSkillsLearned(clean(r.skillsLearned()));
        e.setChallenges(clean(r.challenges()));
        return LogbookResponse.from(entries.save(e));
    }

    /** Students can edit an entry until the company or supervisor has reviewed it. */
    public LogbookResponse update(Long entryId, AuthUser me, LogbookUpdateRequest r) {
        LogbookEntry e = find(entryId);
        Attachment a = e.getAttachment();
        if (!attachmentService.isStudentOwner(a, me)) {
            throw ApiException.forbidden("This is not your logbook entry");
        }
        if (a.getStatus() != AttachmentStatus.ONGOING) {
            throw ApiException.conflict("This attachment is already completed");
        }
        if (e.isReviewed()) {
            throw ApiException.conflict("This entry has already been reviewed and can no longer be edited");
        }
        e.setActivities(r.activities().trim());
        e.setSkillsLearned(clean(r.skillsLearned()));
        e.setChallenges(clean(r.challenges()));
        e.setUpdatedAt(LocalDateTime.now());
        return LogbookResponse.from(entries.save(e));
    }

    @Transactional(readOnly = true)
    public List<LogbookResponse> list(Long attachmentId, AuthUser me) {
        Attachment a = attachmentService.loadAccessible(attachmentId, me);
        return entries.findByAttachmentIdOrderByEntryDateDesc(a.getId())
                .stream().map(LogbookResponse::from).toList();
    }

    /** The host company or the assigned supervisor adds their comment. */
    public LogbookResponse review(Long entryId, AuthUser me, ReviewRequest r) {
        LogbookEntry e = find(entryId);
        Attachment a = e.getAttachment();
        LocalDateTime now = LocalDateTime.now();
        if (attachmentService.isCompanyOwner(a, me)) {
            e.setCompanyComment(r.comment().trim());
            e.setCompanyReviewedAt(now);
        } else if (attachmentService.isAssignedSupervisor(a, me)) {
            e.setSupervisorComment(r.comment().trim());
            e.setSupervisorReviewedAt(now);
        } else {
            throw ApiException.forbidden("Only the host company or the assigned supervisor can review this entry");
        }
        return LogbookResponse.from(entries.save(e));
    }

    private LogbookEntry find(Long id) {
        return entries.findById(id).orElseThrow(() -> ApiException.notFound("Logbook entry not found"));
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
