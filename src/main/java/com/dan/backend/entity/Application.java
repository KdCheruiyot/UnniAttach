package com.dan.backend.entity;

import com.dan.backend.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A student's application to a placement. */
@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "placement_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_id", nullable = false)
    private Placement placement;

    @Column(length = 4000)
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    private LocalDateTime decidedAt;

    public Application() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Placement getPlacement() { return placement; }
    public void setPlacement(Placement placement) { this.placement = placement; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
