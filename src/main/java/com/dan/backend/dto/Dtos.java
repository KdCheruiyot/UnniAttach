package com.dan.backend.dto;

import com.dan.backend.entity.Application;
import com.dan.backend.entity.Assessment;
import com.dan.backend.entity.Attachment;
import com.dan.backend.entity.LogbookEntry;
import com.dan.backend.entity.Placement;
import com.dan.backend.entity.User;
import com.dan.backend.enums.ApplicationStatus;
import com.dan.backend.enums.AssessmentType;
import com.dan.backend.enums.AttachmentStatus;
import com.dan.backend.enums.PlacementStatus;
import com.dan.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Request and response bodies for everything except authentication. */
public final class Dtos {

    private Dtos() {
    }

    // ---------- Placements ----------

    public record PlacementRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 4000) String description,
            @NotBlank @Size(max = 150) String location,
            @Size(max = 500) String requiredSkills,
            @NotNull @Min(1) Integer slots,
            @NotNull LocalDate deadline,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
    }

    public record PlacementResponse(
            Long id,
            Long companyId,
            String companyName,
            String title,
            String description,
            String location,
            String requiredSkills,
            int slots,
            long acceptedCount,
            LocalDate deadline,
            LocalDate startDate,
            LocalDate endDate,
            PlacementStatus status,
            LocalDateTime createdAt) {

        public static PlacementResponse from(Placement p, long acceptedCount) {
            User c = p.getCompany();
            String name = c.getOrganization() != null && !c.getOrganization().isBlank()
                    ? c.getOrganization() : c.getFullName();
            return new PlacementResponse(p.getId(), c.getId(), name, p.getTitle(), p.getDescription(),
                    p.getLocation(), p.getRequiredSkills(), p.getSlots(), acceptedCount, p.getDeadline(),
                    p.getStartDate(), p.getEndDate(), p.getStatus(), p.getCreatedAt());
        }
    }

    // ---------- Applications ----------

    public record ApplicationRequest(
            @NotNull Long placementId,
            @Size(max = 4000) String coverLetter) {
    }

    public record DecisionRequest(
            @NotNull(message = "must be ACCEPTED or REJECTED") ApplicationStatus status,
            @Size(max = 1000) String remarks) {
    }

    public record ApplicationResponse(
            Long id,
            Long placementId,
            String placementTitle,
            Long studentId,
            String studentName,
            String studentEmail,
            String registrationNumber,
            String course,
            String coverLetter,
            ApplicationStatus status,
            String remarks,
            LocalDateTime appliedAt,
            LocalDateTime decidedAt) {

        public static ApplicationResponse from(Application a) {
            User s = a.getStudent();
            return new ApplicationResponse(a.getId(), a.getPlacement().getId(), a.getPlacement().getTitle(),
                    s.getId(), s.getFullName(), s.getEmail(), s.getRegistrationNumber(), s.getCourse(),
                    a.getCoverLetter(), a.getStatus(), a.getRemarks(), a.getAppliedAt(), a.getDecidedAt());
        }
    }

    // ---------- Attachments ----------

    public record AssignSupervisorRequest(@NotNull Long supervisorId) {
    }

    public record AttachmentResponse(
            Long id,
            Long applicationId,
            Long placementId,
            String placementTitle,
            Long studentId,
            String studentName,
            Long companyId,
            String companyName,
            Long supervisorId,
            String supervisorName,
            LocalDate startDate,
            LocalDate endDate,
            AttachmentStatus status,
            LocalDateTime createdAt,
            LocalDateTime completedAt) {

        public static AttachmentResponse from(Attachment a) {
            User c = a.getCompany();
            User sup = a.getSupervisor();
            String companyName = c.getOrganization() != null && !c.getOrganization().isBlank()
                    ? c.getOrganization() : c.getFullName();
            return new AttachmentResponse(a.getId(), a.getApplication().getId(), a.getPlacement().getId(),
                    a.getPlacement().getTitle(), a.getStudent().getId(), a.getStudent().getFullName(),
                    c.getId(), companyName, sup == null ? null : sup.getId(),
                    sup == null ? null : sup.getFullName(), a.getStartDate(), a.getEndDate(),
                    a.getStatus(), a.getCreatedAt(), a.getCompletedAt());
        }
    }

    // ---------- Logbook ----------

    public record LogbookRequest(
            @NotNull LocalDate entryDate,
            @NotBlank @Size(max = 4000) String activities,
            @Size(max = 2000) String skillsLearned,
            @Size(max = 2000) String challenges) {
    }

    public record LogbookUpdateRequest(
            @NotBlank @Size(max = 4000) String activities,
            @Size(max = 2000) String skillsLearned,
            @Size(max = 2000) String challenges) {
    }

    public record ReviewRequest(@NotBlank @Size(max = 2000) String comment) {
    }

    public record LogbookResponse(
            Long id,
            Long attachmentId,
            LocalDate entryDate,
            String activities,
            String skillsLearned,
            String challenges,
            String companyComment,
            LocalDateTime companyReviewedAt,
            String supervisorComment,
            LocalDateTime supervisorReviewedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        public static LogbookResponse from(LogbookEntry e) {
            return new LogbookResponse(e.getId(), e.getAttachment().getId(), e.getEntryDate(),
                    e.getActivities(), e.getSkillsLearned(), e.getChallenges(), e.getCompanyComment(),
                    e.getCompanyReviewedAt(), e.getSupervisorComment(), e.getSupervisorReviewedAt(),
                    e.getCreatedAt(), e.getUpdatedAt());
        }
    }

    // ---------- Assessments ----------

    public record AssessmentRequest(
            @NotNull @Min(0) @Max(100) Integer score,
            @NotBlank @Size(max = 2000) String comments) {
    }

    public record AssessmentResponse(
            Long id,
            Long attachmentId,
            Long assessorId,
            String assessorName,
            AssessmentType type,
            int score,
            String comments,
            LocalDateTime createdAt) {

        public static AssessmentResponse from(Assessment a) {
            return new AssessmentResponse(a.getId(), a.getAttachment().getId(), a.getAssessor().getId(),
                    a.getAssessor().getFullName(), a.getType(), a.getScore(), a.getComments(),
                    a.getCreatedAt());
        }
    }

    // ---------- Admin ----------

    public record AdminCreateUserRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") String password,
            @NotNull Role role,
            @Size(max = 30) String phone,
            @Size(max = 150) String organization) {
    }

    public record EnabledRequest(@NotNull Boolean enabled) {
    }

    public record StatsResponse(
            Map<String, Long> usersByRole,
            long placements,
            long openPlacements,
            long applications,
            long ongoingAttachments,
            long completedAttachments) {
    }
}
