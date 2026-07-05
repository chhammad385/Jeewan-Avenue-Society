package com.jeewanavenue.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // The default @JsonFormat ensures string output. Adjust pattern/timezone if needed.
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDatetime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDatetime;

    public Announcement() {
    }

    public Announcement(Long id, String title, String content, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }

    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }
}