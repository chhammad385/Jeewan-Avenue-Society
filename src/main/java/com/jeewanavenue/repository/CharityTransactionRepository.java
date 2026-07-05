package com.jeewanavenue.repository;

import com.jeewanavenue.entity.CharityTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CharityTransactionRepository extends JpaRepository<CharityTransaction, Long> {
    
    // Find all transactions by type (Income/Expense)
    List<CharityTransaction> findByTypeOrderByDateDesc(String type);
    
    // Find transactions by type and date range
    @Query("SELECT ct FROM CharityTransaction ct WHERE ct.type = :type AND ct.date BETWEEN :startDate AND :endDate ORDER BY ct.date DESC")
    List<CharityTransaction> findByTypeAndDateBetween(
        @Param("type") String type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Find transactions by date range
    @Query("SELECT ct FROM CharityTransaction ct WHERE ct.date BETWEEN :startDate AND :endDate ORDER BY ct.date DESC")
    List<CharityTransaction> findByDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Find transactions by month
    List<CharityTransaction> findByMonthOrderByDateDesc(String month);
    
    // Calculate total income
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CharityTransaction ct WHERE ct.type = 'Income'")
    BigDecimal calculateTotalIncome();
    
    // Calculate total expenses
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CharityTransaction ct WHERE ct.type = 'Expense'")
    BigDecimal calculateTotalExpense();
    
    // Calculate income by date range
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CharityTransaction ct WHERE ct.type = 'Income' AND ct.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateIncomeByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Calculate expense by date range
    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CharityTransaction ct WHERE ct.type = 'Expense' AND ct.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateExpenseByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
