package com.jeewanavenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "financials")
public class Financial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "Income" or "Expense"
    private LocalDate date;
    private String month; // Payment month (for Monthly Fees tracking)
    private String description;
    private String category;
    private BigDecimal amount;
    private String paymentMethod;
    private String plotNo;
    private String phoneNo;
    private String receiptPath;
    private Boolean isCharity; // Flag to indicate if this is a charity donation

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPlotNo() { return plotNo; }
    public void setPlotNo(String plotNo) { this.plotNo = plotNo; }
    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
    public String getReceiptPath() { return receiptPath; }
    public void setReceiptPath(String receiptPath) { this.receiptPath = receiptPath; }
    public Boolean getIsCharity() { return isCharity; }
    public void setIsCharity(Boolean isCharity) { this.isCharity = isCharity; }
}