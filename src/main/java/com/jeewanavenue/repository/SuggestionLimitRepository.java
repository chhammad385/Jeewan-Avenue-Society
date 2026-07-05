package com.jeewanavenue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jeewanavenue.entity.SuggestionLimit;
import com.jeewanavenue.entity.User;

@Repository
public interface SuggestionLimitRepository extends JpaRepository<SuggestionLimit, Long> {

    // Find limit for specific user and month
    Optional<SuggestionLimit> findByUserAndMonthYear(User user, String monthYear);

    // Find all limits for a user
    List<SuggestionLimit> findByUserOrderByMonthYearDesc(User user);

    // Find limits for current month for all users
    List<SuggestionLimit> findByMonthYear(String monthYear);

    // Check if user has reached limit for specific month
    @Query("SELECT sl.suggestionCount >= sl.maxSuggestions FROM SuggestionLimit sl " +
           "WHERE sl.user = :user AND sl.monthYear = :monthYear")
    Optional<Boolean> hasReachedLimit(@Param("user") User user, @Param("monthYear") String monthYear);

    // Get remaining suggestions for user in specific month
    @Query("SELECT (sl.maxSuggestions - sl.suggestionCount) FROM SuggestionLimit sl " +
           "WHERE sl.user = :user AND sl.monthYear = :monthYear")
    Optional<Integer> getRemainingCount(@Param("user") User user, @Param("monthYear") String monthYear);

    // Get statistics for admin dashboard
    @Query("SELECT sl.monthYear, SUM(sl.suggestionCount), AVG(sl.suggestionCount) " +
           "FROM SuggestionLimit sl GROUP BY sl.monthYear ORDER BY sl.monthYear DESC")
    List<Object[]> getMonthlyStatistics();

    // Find users who have reached their monthly limit
    @Query("SELECT sl FROM SuggestionLimit sl WHERE sl.monthYear = :monthYear " +
           "AND sl.suggestionCount >= sl.maxSuggestions")
    List<SuggestionLimit> findUsersAtLimit(@Param("monthYear") String monthYear);

    // Clean up old records (older than specified months)
    @Query("DELETE FROM SuggestionLimit sl WHERE sl.monthYear < :cutoffMonth")
    void deleteOldRecords(@Param("cutoffMonth") String cutoffMonth);
}