package com.dan.backend.entity;

import com.dan.backend.enums.PlacementStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** An attachment opportunity posted by a company. */
@Entity
@Table(name = "placements")
public class Placement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(length = 500)
    private String requiredSkills;

    @Column(nullable = false)
    private int slots;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlacementStatus status = PlacementStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Placement() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCompany() { return company; }
    public void setCompany(User company) { this.company = company; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public int getSlots() { return slots; }
    public void setSlots(int slots) { this.slots = slots; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public PlacementStatus getStatus() { return status; }
    public void setStatus(PlacementStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
