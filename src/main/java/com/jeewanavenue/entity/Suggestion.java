package com.jeewanavenue.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "user_name", nullable = false)
    private String userName; // Denormalized for easier querying

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Default constructor
    public Suggestion() {
        this.submittedAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructor with required fields
    public Suggestion(String title, String description, Category category, Priority priority, User user) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.user = user;
        this.userName = user.getOwnerName();
    }

    // Enums
    public enum Category {
        FACILITY("facility"),
        SECURITY("security"),
        MAINTENANCE("maintenance"),
        COMMUNICATION("communication"),
        FINANCIAL("financial"),
        OTHER("other");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public enum Priority {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low");

        private final String value;

        Priority(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userName = user.getOwnerName();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", userName='" + userName + '\'' +
                ", submittedAt=" + submittedAt +
                ", isRead=" + isRead +
                '}';
    }
}