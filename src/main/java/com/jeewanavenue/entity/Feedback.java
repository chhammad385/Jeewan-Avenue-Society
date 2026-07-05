package com.jeewanavenue.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String personName;
    private boolean isSatisfied;
    private String comments;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "time_zone")
    private String timeZone;

    // Constructor that sets current timestamp
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (timeZone == null) {
            timeZone = "UTC";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public boolean getIsSatisfied() { return isSatisfied; }
    public void setIsSatisfied(boolean isSatisfied) { this.isSatisfied = isSatisfied; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Long getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(Long submittedByUserId) { this.submittedByUserId = submittedByUserId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}