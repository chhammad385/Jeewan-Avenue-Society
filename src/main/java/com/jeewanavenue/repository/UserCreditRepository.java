package com.jeewanavenue.repository;

import com.jeewanavenue.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {
    // Find credits by user ID
    List<UserCredit> findByUserId(Long userId);
    
    // Find credits by plot number
    List<UserCredit> findByPlotNo(String plotNo);
    
    // Find all users with positive credit balance
    @Query("SELECT uc FROM UserCredit uc WHERE uc.creditAmount > 0 ORDER BY uc.creditAmount DESC")
    List<UserCredit> findAllWithPositiveCredit();
    
    // Find users with credit amount greater than specified value
    List<UserCredit> findByCreditAmountGreaterThan(BigDecimal amount);
    
    // Get total credit amount for a user
    @Query("SELECT COALESCE(SUM(uc.creditAmount), 0) FROM UserCredit uc WHERE uc.userId = :userId")
    BigDecimal getTotalCreditByUserId(@Param("userId") Long userId);
    
    // Get total credit amount by plot number
    @Query("SELECT COALESCE(SUM(uc.creditAmount), 0) FROM UserCredit uc WHERE uc.plotNo = :plotNo")
    BigDecimal getTotalCreditByPlotNo(@Param("plotNo") String plotNo);
}