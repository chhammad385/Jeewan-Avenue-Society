package com.jeewanavenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dues")
public class Due {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String plotNo;

    @Column(nullable = false)
    private BigDecimal areaMarla;

    @Column(nullable = false)
    private BigDecimal dueAmount;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "due_month", nullable = false)
    private String dueMonth;

    @Column(name = "due_year", nullable = false)
    private Integer dueYear;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private Integer gapDays;

    @Column(nullable = false)
    private BigDecimal lateCharges;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean lateChargesApplied = false;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPlotNo() { return plotNo; }
    public void setPlotNo(String plotNo) { this.plotNo = plotNo; }
    public BigDecimal getAreaMarla() { return areaMarla; }
    public void setAreaMarla(BigDecimal areaMarla) { this.areaMarla = areaMarla; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getDueMonth() { return dueMonth; }
    public void setDueMonth(String dueMonth) { this.dueMonth = dueMonth; }
    public Integer getDueYear() { return dueYear; }
    public void setDueYear(Integer dueYear) { this.dueYear = dueYear; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public Integer getGapDays() { return gapDays; }
    public void setGapDays(Integer gapDays) { this.gapDays = gapDays; }
    public BigDecimal getLateCharges() { return lateCharges; }
    public void setLateCharges(BigDecimal lateCharges) { this.lateCharges = lateCharges; }
    public Boolean getLateChargesApplied() { return lateChargesApplied; }
    public void setLateChargesApplied(Boolean lateChargesApplied) { this.lateChargesApplied = lateChargesApplied; }
}
