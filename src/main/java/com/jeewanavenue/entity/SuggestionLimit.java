package com.jeewanavenue.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "suggestion_limits", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "month_year"}))
public class SuggestionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month_year", nullable = false)
    private String monthYear; // Format: "YYYY-MM" (e.g., "2024-03")

    @Column(name = "suggestion_count", nullable = false)
    private Integer suggestionCount = 0;

    @Column(name = "max_suggestions", nullable = false)
    private Integer maxSuggestions = 5; // Default limit per month

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    // Default constructor
    public SuggestionLimit() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.suggestionCount = 0;
        this.maxSuggestions = 5;
    }

    // Constructor with required fields
    public SuggestionLimit(User user, String monthYear) {
        this();
        this.user = user;
        this.monthYear = monthYear;
    }

    // Helper method to check if user can submit more suggestions
    public boolean canSubmitMore() {
        return suggestionCount < maxSuggestions;
    }

    // Helper method to get remaining suggestions for the month
    public int getRemainingCount() {
        return Math.max(0, maxSuggestions - suggestionCount);
    }

    // Helper method to increment suggestion count
    public void incrementCount() {
        this.suggestionCount++;
        this.updatedAt = LocalDate.now();
    }

    // Static helper method to generate monthYear string
    public static String getCurrentMonthYear() {
        LocalDate now = LocalDate.now();
        return String.format("%04d-%02d", now.getYear(), now.getMonthValue());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public Integer getSuggestionCount() {
        return suggestionCount;
    }

    public void setSuggestionCount(Integer suggestionCount) {
        this.suggestionCount = suggestionCount;
        this.updatedAt = LocalDate.now();
    }

    public Integer getMaxSuggestions() {
        return maxSuggestions;
    }

    public void setMaxSuggestions(Integer maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        this.updatedAt = LocalDate.now();
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SuggestionLimit{" +
                "id=" + id +
                ", monthYear='" + monthYear + '\'' +
                ", suggestionCount=" + suggestionCount +
                ", maxSuggestions=" + maxSuggestions +
                ", remainingCount=" + getRemainingCount() +
                '}';
    }
}