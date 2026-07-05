package com.jeewanavenue.service;

import com.jeewanavenue.entity.AccountTransaction;
import com.jeewanavenue.entity.AccountTransaction.TransactionType;
import com.jeewanavenue.repository.AccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountTransactionService {
    
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;
    
    /**
     * Log a monthly payment due transaction
     */
    public AccountTransaction logMonthlyPaymentDue(Long userId, BigDecimal amount, String monthYear) {
        String description = String.format("Monthly payment due added for %s - Amount: Rs. %s", 
                                          monthYear, amount);
        
        AccountTransaction transaction = new AccountTransaction(
            userId, amount, TransactionType.MONTHLY_PAYMENT_DUE, description, null
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Log a custom due addition
     */
    public AccountTransaction logCustomDueAdded(Long userId, BigDecimal amount, 
                                               String description, Long customDueId) {
        String transactionDesc = String.format("Custom due added: %s - Amount: Rs. %s", 
                                              description, amount);
        
        AccountTransaction transaction = new AccountTransaction(
            userId, amount, TransactionType.CUSTOM_DUE_ADDED, transactionDesc, customDueId
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Log a custom due payment
     */
    public AccountTransaction logCustomDuePaid(Long userId, BigDecimal amount, 
                                              String description, Long customDueId) {
        String transactionDesc = String.format("Custom due paid: %s - Amount: Rs. %s", 
                                              description, amount);
        
        // Payment reduces balance, so amount should be negative
        AccountTransaction transaction = new AccountTransaction(
            userId, amount.negate(), TransactionType.CUSTOM_DUE_PAID, transactionDesc, customDueId
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Log an income payment received
     */
    public AccountTransaction logIncomePayment(Long userId, BigDecimal amount, 
                                             String description, Long financialId) {
        String transactionDesc = String.format("Income payment: %s - Amount: Rs. %s", 
                                              description, amount);
        
        // Income payment reduces balance (pays off dues), so amount should be negative
        AccountTransaction transaction = new AccountTransaction(
            userId, amount.negate(), TransactionType.INCOME_PAYMENT, transactionDesc, financialId
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Log an income payment edit
     */
    public AccountTransaction logIncomePaymentEdit(Long userId, BigDecimal oldAmount, 
                                                  BigDecimal newAmount, String description, 
                                                  Long financialId) {
        BigDecimal difference = newAmount.subtract(oldAmount);
        String transactionDesc = String.format("Income payment edited: %s - Difference: Rs. %s", 
                                              description, difference);
        
        // If difference is positive, it means less payment (increase balance)
        // If difference is negative, it means more payment (decrease balance)
        AccountTransaction transaction = new AccountTransaction(
            userId, difference.negate(), TransactionType.INCOME_PAYMENT_EDIT, 
            transactionDesc, financialId
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Log an income payment deletion
     */
    public AccountTransaction logIncomePaymentDeleted(Long userId, BigDecimal amount, 
                                                    String description, Long financialId) {
        String transactionDesc = String.format("Income payment deleted: %s - Amount: Rs. %s", 
                                              description, amount);
        
        // Deleting payment increases balance (adds back to dues), so amount should be positive
        AccountTransaction transaction = new AccountTransaction(
            userId, amount, TransactionType.INCOME_PAYMENT_DELETED, transactionDesc, financialId
        );
        
        return accountTransactionRepository.save(transaction);
    }
    
    /**
     * Get all transactions for a user
     */
    public List<AccountTransaction> getUserTransactions(Long userId) {
        return accountTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }
    
    /**
     * Get recent transactions for a user (last N transactions)
     */
    public List<AccountTransaction> getRecentUserTransactions(Long userId, int limit) {
        return accountTransactionRepository.findRecentTransactionsByUserId(userId)
                .stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * Get transactions for a user within date range
     */
    public List<AccountTransaction> getUserTransactionsByDateRange(Long userId, 
                                                                 LocalDateTime startDate, 
                                                                 LocalDateTime endDate) {
        return accountTransactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * Get transactions by type for a user
     */
    public List<AccountTransaction> getUserTransactionsByType(Long userId, TransactionType type) {
        return accountTransactionRepository.findByUserIdAndTransactionTypeOrderByTransactionDateDesc(userId, type);
    }
    
    /**
     * Get total number of transactions for a user
     */
    public long getUserTransactionCount(Long userId) {
        return accountTransactionRepository.countByUserId(userId);
    }
    
    /**
     * Find transactions related to a financial record
     */
    public List<AccountTransaction> getTransactionsByFinancialId(Long financialId) {
        return accountTransactionRepository.findByFinancialRecordId(financialId);
    }
}