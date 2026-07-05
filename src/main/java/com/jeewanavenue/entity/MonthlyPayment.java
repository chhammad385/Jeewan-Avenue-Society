package com.jeewanavenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "monthly_payments")
public class MonthlyPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal ratePerMarla;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private Integer gapDays;

    @Column(nullable = false)
    private BigDecimal lateCharges;

    @Column(nullable = false)
    private LocalDate lastDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy; // User ID who created this payment setting

    @Column(name = "property_type")
    private String propertyType; // Property Type filter (House, Shop)

    @Column(name = "built_status")
    private String builtStatus; // Built status filter (Built, Under Construction, Not Built)

    // Automatically set created/updated timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastDate == null && issueDate != null && gapDays != null) {
            lastDate = issueDate.plusDays(gapDays);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (lastDate == null && issueDate != null && gapDays != null) {
            lastDate = issueDate.plusDays(gapDays);
        }
    }

    // Constructors
    public MonthlyPayment() {}

    public MonthlyPayment(BigDecimal ratePerMarla, Integer month, Integer year, 
                         LocalDate issueDate, Integer gapDays, BigDecimal lateCharges, Long createdBy) {
        this.ratePerMarla = ratePerMarla;
        this.month = month;
        this.year = year;
        this.issueDate = issueDate;
        this.gapDays = gapDays;
        this.lateCharges = lateCharges;
        this.createdBy = createdBy;
        this.lastDate = issueDate.plusDays(gapDays);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getRatePerMarla() { return ratePerMarla; }
    public void setRatePerMarla(BigDecimal ratePerMarla) { this.ratePerMarla = ratePerMarla; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public Integer getGapDays() { return gapDays; }
    public void setGapDays(Integer gapDays) { this.gapDays = gapDays; }

    public BigDecimal getLateCharges() { return lateCharges; }
    public void setLateCharges(BigDecimal lateCharges) { this.lateCharges = lateCharges; }

    public LocalDate getLastDate() { return lastDate; }
    public void setLastDate(LocalDate lastDate) { this.lastDate = lastDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public String getBuiltStatus() { return builtStatus; }
    public void setBuiltStatus(String builtStatus) { this.builtStatus = builtStatus; }
}
