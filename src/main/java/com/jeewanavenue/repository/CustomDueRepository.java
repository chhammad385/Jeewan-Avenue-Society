package com.jeewanavenue.repository;

import com.jeewanavenue.entity.CustomDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CustomDueRepository extends JpaRepository<CustomDue, Long> {
    
    /**
     * Find all custom dues for a specific user
     */
    List<CustomDue> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find all unpaid custom dues for a specific user
     */
    List<CustomDue> findByUserIdAndIsPaidFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find custom dues by plot number
     */
    List<CustomDue> findByPlotNoOrderByCreatedAtDesc(String plotNo);
    
    /**
     * Find custom dues by category
     */
    List<CustomDue> findByCategoryOrderByCreatedAtDesc(String category);
    
    /**
     * Calculate total unpaid custom dues for a specific user
     */
    @Query("SELECT COALESCE(SUM(cd.amount), 0) FROM CustomDue cd WHERE cd.userId = :userId AND cd.isPaid = false")
    BigDecimal getTotalUnpaidAmountByUserId(@Param("userId") Long userId);
    
    /**
     * Calculate total custom dues (paid and unpaid) for a specific user
     */
    @Query("SELECT COALESCE(SUM(cd.amount), 0) FROM CustomDue cd WHERE cd.userId = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);
    
    /**
     * Find all custom dues created by a specific admin/staff
     */
    List<CustomDue> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Find custom dues within a date range
     */
    @Query("SELECT cd FROM CustomDue cd WHERE cd.createdAt BETWEEN :startDate AND :endDate ORDER BY cd.createdAt DESC")
    List<CustomDue> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                          @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Get all unpaid custom dues across all users
     */
    List<CustomDue> findByIsPaidFalseOrderByCreatedAtDesc();
    
    /**
     * Count unpaid custom dues for a user
     */
    @Query("SELECT COUNT(cd) FROM CustomDue cd WHERE cd.userId = :userId AND cd.isPaid = false")
    Long countUnpaidByUserId(@Param("userId") Long userId);
}