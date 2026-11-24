package com.dan.backend.entity;

import com.dan.backend.enums.AssessmentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A score given to the student by the company or by the university supervisor. */
@Entity
@Table(name = "assessments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attachment_id", "assessment_type"}))
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessor_id", nullable = false)
    private User assessor;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 20)
    private AssessmentType type;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 2000)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Assessment() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Attachment getAttachment() { return attachment; }
    public void setAttachment(Attachment attachment) { this.attachment = attachment; }
    public User getAssessor() { return assessor; }
    public void setAssessor(User assessor) { this.assessor = assessor; }
    public AssessmentType getType() { return type; }
    public void setType(AssessmentType type) { this.type = type; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
