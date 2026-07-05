package com.jeewanavenue.repository;

import com.jeewanavenue.entity.AccountTransaction;
import com.jeewanavenue.entity.AccountTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    
    /**
     * Find all transactions for a specific user, ordered by transaction date descending
     */
    List<AccountTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    
    /**
     * Find transactions for a user within a date range
     */
    @Query("SELECT at FROM AccountTransaction at WHERE at.userId = :userId " +
           "AND at.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY at.transactionDate DESC")
    List<AccountTransaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find transactions by user and transaction type
     */
    List<AccountTransaction> findByUserIdAndTransactionTypeOrderByTransactionDateDesc(
        Long userId, TransactionType transactionType
    );
    
    /**
     * Find transactions by reference ID and type (useful for finding related transactions)
     */
    List<AccountTransaction> findByReferenceIdAndTransactionType(
        Long referenceId, TransactionType transactionType
    );
    
    /**
     * Get recent transactions for a user (last N transactions)
     */
    @Query("SELECT at FROM AccountTransaction at WHERE at.userId = :userId " +
           "ORDER BY at.transactionDate DESC")
    List<AccountTransaction> findRecentTransactionsByUserId(@Param("userId") Long userId);
    
    /**
     * Count total transactions for a user
     */
    long countByUserId(Long userId);
    
    /**
     * Find transactions that reference a specific financial record
     */
    @Query("SELECT at FROM AccountTransaction at WHERE at.referenceId = :referenceId " +
           "AND at.transactionType IN ('INCOME_PAYMENT', 'INCOME_PAYMENT_EDIT', 'INCOME_PAYMENT_DELETED') " +
           "ORDER BY at.transactionDate DESC")
    List<AccountTransaction> findByFinancialRecordId(@Param("referenceId") Long referenceId);
}