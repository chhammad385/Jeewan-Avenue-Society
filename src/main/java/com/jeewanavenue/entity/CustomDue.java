package com.jeewanavenue.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "custom_dues")
public class CustomDue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String plotNo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private String category; // e.g., "Fine", "Extra Charge", "Special Assessment", etc.

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy; // Admin/Staff who added this due

    @Column(nullable = true)
    private LocalDateTime dueDate; // Optional due date

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPaid = false;

    @Column(nullable = true)
    private LocalDateTime paidAt;

    @Column(nullable = true)
    private String notes; // Additional notes

    // Constructors
    public CustomDue() {
        this.createdAt = LocalDateTime.now();
    }

    public CustomDue(Long userId, String userName, String plotNo, BigDecimal amount, 
                     String reason, String category, String createdBy) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.plotNo = plotNo;
        this.amount = amount;
        this.reason = reason;
        this.category = category;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPlotNo() { return plotNo; }
    public void setPlotNo(String plotNo) { this.plotNo = plotNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
