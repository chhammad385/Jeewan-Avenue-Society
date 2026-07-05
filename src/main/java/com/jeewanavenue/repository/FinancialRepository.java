package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Financial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- Import this

@Repository
public interface FinancialRepository extends JpaRepository<Financial, Long> {
    // --- ADD THIS NEW METHOD ---
    List<Financial> findByPlotNo(String plotNo);
    
    @Query("SELECT f FROM Financial f WHERE MONTH(f.date) = :month AND YEAR(f.date) = :year")
    List<Financial> findIncomeByMonthAndYear(@Param("month") int month, @Param("year") int year);
}