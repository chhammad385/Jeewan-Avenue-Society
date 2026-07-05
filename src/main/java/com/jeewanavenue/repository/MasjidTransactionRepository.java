package com.jeewanavenue.repository;

import com.jeewanavenue.entity.MasjidTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MasjidTransactionRepository extends JpaRepository<MasjidTransaction, Long> {
    
    // Find all transactions by type (Income/Expense)
    List<MasjidTransaction> findByTypeOrderByDateDesc(String type);
    
    // Find transactions by type and date range
    @Query("SELECT mt FROM MasjidTransaction mt WHERE mt.type = :type AND mt.date BETWEEN :startDate AND :endDate ORDER BY mt.date DESC")
    List<MasjidTransaction> findByTypeAndDateBetween(
        @Param("type") String type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Find transactions by date range
    @Query("SELECT mt FROM MasjidTransaction mt WHERE mt.date BETWEEN :startDate AND :endDate ORDER BY mt.date DESC")
    List<MasjidTransaction> findByDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Calculate total income
    @Query("SELECT COALESCE(SUM(mt.amount), 0) FROM MasjidTransaction mt WHERE mt.type = 'Income'")
    BigDecimal calculateTotalIncome();
    
    // Calculate total expense
    @Query("SELECT COALESCE(SUM(mt.amount), 0) FROM MasjidTransaction mt WHERE mt.type = 'Expense'")
    BigDecimal calculateTotalExpense();
    
    // Calculate total income within date range
    @Query("SELECT COALESCE(SUM(mt.amount), 0) FROM MasjidTransaction mt WHERE mt.type = 'Income' AND mt.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalIncomeByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Calculate total expense within date range
    @Query("SELECT COALESCE(SUM(mt.amount), 0) FROM MasjidTransaction mt WHERE mt.type = 'Expense' AND mt.date BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalExpenseByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Find by category
    List<MasjidTransaction> findByCategoryOrderByDateDesc(String category);
    
    // Find all transactions ordered by date descending
    List<MasjidTransaction> findAllByOrderByDateDesc();
}
