package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Due;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DueRepository extends JpaRepository<Due, Long> {
    List<Due> findByUserId(Long userId);
    List<Due> findByMonthAndYear(Integer month, Integer year);
    List<Due> findByUserNameContainingIgnoreCase(String userName);
    List<Due> findByMonthAndYearAndUserNameContainingIgnoreCase(Integer month, Integer year, String userName);
    
    // Find existing due for specific user, month, and year
    Due findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
    
    // Find all dues with amount greater than specified value (for finding unpaid dues)
    List<Due> findByDueAmountGreaterThan(BigDecimal amount);
    
    // Find unpaid dues that haven't had late charges applied yet
    List<Due> findByDueAmountGreaterThanAndLateChargesAppliedFalse(BigDecimal amount);
}
