package com.jeewanavenue.repository;

import com.jeewanavenue.entity.UserDebit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDebitRepository extends JpaRepository<UserDebit, Long> {
    // Find debit by user ID
    Optional<UserDebit> findByUserId(Long userId);
    
    // Find debit by plot number
    Optional<UserDebit> findByPlotNo(String plotNo);
    
    // Find all users with positive debit balance
    @Query("SELECT ud FROM UserDebit ud WHERE ud.debitAmount > 0 ORDER BY ud.debitAmount DESC")
    List<UserDebit> findAllWithPositiveDebit();
    
    // Find users with debit amount greater than specified value
    List<UserDebit> findByDebitAmountGreaterThan(BigDecimal amount);
    
    // Get total debit amount for a user
    @Query("SELECT ud.debitAmount FROM UserDebit ud WHERE ud.userId = :userId")
    BigDecimal getTotalDebitByUserId(@Param("userId") Long userId);
    
    // Get total debit amount by plot number
    @Query("SELECT ud.debitAmount FROM UserDebit ud WHERE ud.plotNo = :plotNo")
    BigDecimal getTotalDebitByPlotNo(@Param("plotNo") String plotNo);
}