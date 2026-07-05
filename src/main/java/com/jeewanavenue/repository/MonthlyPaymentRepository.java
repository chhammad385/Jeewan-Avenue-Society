package com.jeewanavenue.repository;

import com.jeewanavenue.entity.MonthlyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {
    // Find existing monthly payment for specific month and year
    Optional<MonthlyPayment> findByMonthAndYear(Integer month, Integer year);
    
    // Find monthly payment for specific month, year, and built status
    Optional<MonthlyPayment> findByMonthAndYearAndBuiltStatus(Integer month, Integer year, String builtStatus);
    
    // Find all monthly payments ordered by year and month (most recent first)
    List<MonthlyPayment> findAllByOrderByYearDescMonthDesc();
    
    // Find monthly payments by year
    List<MonthlyPayment> findByYear(Integer year);
    
    // Find all monthly payments for a specific month and year
    List<MonthlyPayment> findAllByMonthAndYear(Integer month, Integer year);
    
    // Find monthly payments by created user
    List<MonthlyPayment> findByCreatedBy(Long createdBy);
}
