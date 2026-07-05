package com.jeewanavenue.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transactions")
public class AccountTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID of the related record (Financial, CustomDue, etc.)
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    public enum TransactionType {
        MONTHLY_PAYMENT_DUE("Monthly Payment Due Added"),
        CUSTOM_DUE_ADDED("Custom Due Added"),
        CUSTOM_DUE_PAID("Custom Due Paid"),
        INCOME_PAYMENT("Income Payment Received"),
        INCOME_PAYMENT_EDIT("Income Payment Edited"),
        INCOME_PAYMENT_DELETED("Income Payment Deleted");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public AccountTransaction() {
        this.transactionDate = LocalDateTime.now();
    }
    
    // Constructor for creating transactions
    public AccountTransaction(Long userId, BigDecimal amount, TransactionType transactionType, 
                            String description, Long referenceId) {
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.referenceId = referenceId;
        this.transactionDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Helper method to get formatted amount with sign
    public String getFormattedAmount() {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + amount.toString();
        } else {
            return amount.toString();
        }
    }
}