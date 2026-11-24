package com.dan.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One day of the student's logbook, reviewed by the company and the supervisor. */
@Entity
@Table(name = "logbook_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attachment_id", "entry_date"}))
public class LogbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 4000)
    private String activities;

    @Column(length = 2000)
    private String skillsLearned;

    @Column(length = 2000)
    private String challenges;

    @Column(length = 2000)
    private String companyComment;

    private LocalDateTime companyReviewedAt;

    @Column(length = 2000)
    private String supervisorComment;

    private LocalDateTime supervisorReviewedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public LogbookEntry() {
    }

    public boolean isReviewed() {
        return companyReviewedAt != null || supervisorReviewedAt != null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Attachment getAttachment() { return attachment; }
    public void setAttachment(Attachment attachment) { this.attachment = attachment; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }
    public String getSkillsLearned() { return skillsLearned; }
    public void setSkillsLearned(String skillsLearned) { this.skillsLearned = skillsLearned; }
    public String getChallenges() { return challenges; }
    public void setChallenges(String challenges) { this.challenges = challenges; }
    public String getCompanyComment() { return companyComment; }
    public void setCompanyComment(String companyComment) { this.companyComment = companyComment; }
    public LocalDateTime getCompanyReviewedAt() { return companyReviewedAt; }
    public void setCompanyReviewedAt(LocalDateTime companyReviewedAt) { this.companyReviewedAt = companyReviewedAt; }
    public String getSupervisorComment() { return supervisorComment; }
    public void setSupervisorComment(String supervisorComment) { this.supervisorComment = supervisorComment; }
    public LocalDateTime getSupervisorReviewedAt() { return supervisorReviewedAt; }
    public void setSupervisorReviewedAt(LocalDateTime supervisorReviewedAt) { this.supervisorReviewedAt = supervisorReviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
