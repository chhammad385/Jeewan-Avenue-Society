package com.jeewanavenue.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jeewanavenue.entity.Suggestion;
import com.jeewanavenue.entity.User;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    // Find suggestions by user
    List<Suggestion> findByUserOrderBySubmittedAtDesc(User user);

    // Find unread suggestions
    List<Suggestion> findByIsReadFalseOrderBySubmittedAtDesc();

    // Count unread suggestions
    long countByIsReadFalse();

    // Find suggestions by category
    List<Suggestion> findByCategoryOrderBySubmittedAtDesc(Suggestion.Category category);

    // Find suggestions by priority
    List<Suggestion> findByPriorityOrderBySubmittedAtDesc(Suggestion.Priority priority);

    // Find suggestions by category and priority
    List<Suggestion> findByCategoryAndPriorityOrderBySubmittedAtDesc(
            Suggestion.Category category, 
            Suggestion.Priority priority
    );

    // Count suggestions submitted by user in current month
    @Query("SELECT COUNT(s) FROM Suggestion s WHERE s.user = :user " +
           "AND s.submittedAt >= :startOfMonth AND s.submittedAt < :startOfNextMonth")
    long countByUserInMonth(@Param("user") User user, 
                           @Param("startOfMonth") LocalDateTime startOfMonth,
                           @Param("startOfNextMonth") LocalDateTime startOfNextMonth);

    // Find suggestions with filters and sorting
    @Query("SELECT s FROM Suggestion s WHERE " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:priority IS NULL OR s.priority = :priority) AND " +
           "(:isRead IS NULL OR s.isRead = :isRead) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'newest' THEN s.submittedAt END DESC, " +
           "CASE WHEN :sortBy = 'oldest' THEN s.submittedAt END ASC, " +
           "CASE WHEN :sortBy = 'priority' THEN " +
           "  CASE s.priority " +
           "    WHEN 'HIGH' THEN 1 " +
           "    WHEN 'MEDIUM' THEN 2 " +
           "    WHEN 'LOW' THEN 3 " +
           "  END " +
           "END ASC")
    List<Suggestion> findWithFilters(@Param("category") Suggestion.Category category,
                                   @Param("priority") Suggestion.Priority priority,
                                   @Param("isRead") Boolean isRead,
                                   @Param("sortBy") String sortBy);

    // Find recent suggestions (last 30 days)
    @Query("SELECT s FROM Suggestion s WHERE s.submittedAt >= :since ORDER BY s.submittedAt DESC")
    List<Suggestion> findRecentSuggestions(@Param("since") LocalDateTime since);

    // Get suggestions statistics
    @Query("SELECT COUNT(s), s.category FROM Suggestion s GROUP BY s.category")
    List<Object[]> getSuggestionCountByCategory();

    @Query("SELECT COUNT(s), s.priority FROM Suggestion s GROUP BY s.priority")
    List<Object[]> getSuggestionCountByPriority();
}