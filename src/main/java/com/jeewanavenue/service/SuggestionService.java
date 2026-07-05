package com.jeewanavenue.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeewanavenue.entity.Suggestion;
import com.jeewanavenue.entity.SuggestionLimit;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.SuggestionLimitRepository;
import com.jeewanavenue.repository.SuggestionRepository;
import com.jeewanavenue.repository.UserRepository;

@Service
@Transactional
public class SuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private SuggestionLimitRepository suggestionLimitRepository;

    @Autowired
    private UserRepository userRepository;

    // --- SUGGESTION CRUD OPERATIONS ---

    public List<Suggestion> findAllSuggestions() {
        return suggestionRepository.findAll();
    }

    public List<Suggestion> findUnreadSuggestions() {
        return suggestionRepository.findByIsReadFalseOrderBySubmittedAtDesc();
    }

    public long getUnreadCount() {
        return suggestionRepository.countByIsReadFalse();
    }

    public Optional<Suggestion> findSuggestionById(Long id) {
        return suggestionRepository.findById(id);
    }

    public List<Suggestion> findSuggestionsByUser(User user) {
        return suggestionRepository.findByUserOrderBySubmittedAtDesc(user);
    }

    // --- SUGGESTION SUBMISSION ---

    @Transactional
    public Suggestion submitSuggestion(String userEmail, String title, String description, 
                                     String category, String priority) throws Exception {
        
        // Find user
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Check monthly limit
        if (!canUserSubmitSuggestion(user)) {
            Map<String, Object> limitInfo = getUserMonthlyLimitInfo(user);
            int remaining = (Integer) limitInfo.get("remaining");
            int max = (Integer) limitInfo.get("maxSuggestions");
            throw new RuntimeException(
                String.format("Monthly suggestion limit reached. You have submitted %d/%d suggestions this month.", 
                             max - remaining, max)
            );
        }

        // Parse enums
        Suggestion.Category suggestionCategory;
        Suggestion.Priority suggestionPriority;
        
        try {
            suggestionCategory = Suggestion.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + category);
        }
        
        try {
            suggestionPriority = Suggestion.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid priority: " + priority);
        }

        // Create suggestion
        Suggestion suggestion = new Suggestion(title, description, suggestionCategory, suggestionPriority, user);
        suggestion = suggestionRepository.save(suggestion);

        // Update monthly limit count
        updateUserMonthlyCount(user);

        return suggestion;
    }

    // --- SUGGESTION MANAGEMENT ---

    @Transactional
    public void markAsRead(Long suggestionId) {
        Optional<Suggestion> suggestionOpt = suggestionRepository.findById(suggestionId);
        if (suggestionOpt.isPresent()) {
            Suggestion suggestion = suggestionOpt.get();
            suggestion.setIsRead(true);
            suggestionRepository.save(suggestion);
        }
    }

    @Transactional
    public void markAsUnread(Long suggestionId) {
        Optional<Suggestion> suggestionOpt = suggestionRepository.findById(suggestionId);
        if (suggestionOpt.isPresent()) {
            Suggestion suggestion = suggestionOpt.get();
            suggestion.setIsRead(false);
            suggestion.setReadAt(null);
            suggestionRepository.save(suggestion);
        }
    }

    // --- MONTHLY LIMIT MANAGEMENT ---

    public boolean canUserSubmitSuggestion(User user) {
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
            .orElse(new SuggestionLimit(user, currentMonth));
        
        return limit.canSubmitMore();
    }

    public Map<String, Object> getUserMonthlyLimitInfo(User user) {
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
            .orElse(new SuggestionLimit(user, currentMonth));

        Map<String, Object> info = new HashMap<>();
        info.put("monthYear", currentMonth);
        info.put("suggestionCount", limit.getSuggestionCount());
        info.put("maxSuggestions", limit.getMaxSuggestions());
        info.put("remaining", limit.getRemainingCount());
        info.put("canSubmitMore", limit.canSubmitMore());
        
        return info;
    }

    @Transactional
    private void updateUserMonthlyCount(User user) {
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
            .orElse(new SuggestionLimit(user, currentMonth));
        
        limit.incrementCount();
        suggestionLimitRepository.save(limit);
    }

    // --- FILTERING AND SEARCH ---

    public List<Suggestion> findSuggestionsWithFilters(String category, String priority, Boolean isRead, String sortBy) {
        Suggestion.Category categoryEnum = null;
        Suggestion.Priority priorityEnum = null;
        
        if (category != null && !category.isEmpty()) {
            try {
                categoryEnum = Suggestion.Category.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, ignore filter
            }
        }
        
        if (priority != null && !priority.isEmpty()) {
            try {
                priorityEnum = Suggestion.Priority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid priority, ignore filter
            }
        }
        
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "newest";
        }
        
        return suggestionRepository.findWithFilters(categoryEnum, priorityEnum, isRead, sortBy);
    }

    public List<Suggestion> findRecentSuggestions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return suggestionRepository.findRecentSuggestions(since);
    }

    // --- STATISTICS AND ANALYTICS ---

    public Map<String, Object> getSuggestionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalSuggestions", suggestionRepository.count());
        stats.put("unreadCount", suggestionRepository.countByIsReadFalse());
        
        // Category breakdown
        List<Object[]> categoryStats = suggestionRepository.getSuggestionCountByCategory();
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] row : categoryStats) {
            Long count = (Long) row[0];
            Suggestion.Category category = (Suggestion.Category) row[1];
            categoryMap.put(category.getValue(), count);
        }
        stats.put("byCategory", categoryMap);
        
        // Priority breakdown
        List<Object[]> priorityStats = suggestionRepository.getSuggestionCountByPriority();
        Map<String, Long> priorityMap = new HashMap<>();
        for (Object[] row : priorityStats) {
            Long count = (Long) row[0];
            Suggestion.Priority priority = (Suggestion.Priority) row[1];
            priorityMap.put(priority.getValue(), count);
        }
        stats.put("byPriority", priorityMap);
        
        // Recent activity (last 7 days)
        List<Suggestion> recentSuggestions = findRecentSuggestions(7);
        stats.put("recentCount", recentSuggestions.size());
        
        return stats;
    }

    public Map<String, Object> getMonthlyLimitStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        List<SuggestionLimit> currentLimits = suggestionLimitRepository.findByMonthYear(currentMonth);
        
        stats.put("totalUsers", currentLimits.size());
        stats.put("usersAtLimit", suggestionLimitRepository.findUsersAtLimit(currentMonth).size());
        
        if (!currentLimits.isEmpty()) {
            double avgSuggestions = currentLimits.stream()
                .mapToInt(SuggestionLimit::getSuggestionCount)
                .average()
                .orElse(0.0);
            stats.put("averageSuggestionsPerUser", avgSuggestions);
        } else {
            stats.put("averageSuggestionsPerUser", 0.0);
        }
        
        // Monthly trends
        List<Object[]> monthlyStats = suggestionLimitRepository.getMonthlyStatistics();
        stats.put("monthlyTrends", monthlyStats);
        
        return stats;
    }

    // --- ADMIN FUNCTIONS ---

    @Transactional
    public void updateUserMonthlyLimit(String userEmail, int newLimit) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
            .orElse(new SuggestionLimit(user, currentMonth));
        
        limit.setMaxSuggestions(newLimit);
        suggestionLimitRepository.save(limit);
    }

    @Transactional
    public int markAllSuggestionsAsRead() {
        List<Suggestion> unreadSuggestions = suggestionRepository.findByIsReadFalseOrderBySubmittedAtDesc();
        
        for (Suggestion suggestion : unreadSuggestions) {
            suggestion.setIsRead(true);
        }
        
        suggestionRepository.saveAll(unreadSuggestions);
        return unreadSuggestions.size();
    }

    public List<Map<String, Object>> getAllUserLimitsInfo() {
        List<User> allUsers = userRepository.findAll();
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        
        return allUsers.stream().map(user -> {
            SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
                .orElse(new SuggestionLimit(user, currentMonth));
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("firstName", user.getOwnerName()); // Using ownerName as full name
            userInfo.put("lastName", ""); // No separate last name in User entity
            userInfo.put("email", user.getEmail());
            userInfo.put("suggestionCount", limit.getSuggestionCount());
            userInfo.put("maxSuggestions", limit.getMaxSuggestions());
            userInfo.put("monthYear", currentMonth);
            
            return userInfo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public int updateAllUsersMonthlyLimit(int newLimit) {
        List<User> allUsers = userRepository.findAll();
        String currentMonth = SuggestionLimit.getCurrentMonthYear();
        int updatedCount = 0;
        
        for (User user : allUsers) {
            SuggestionLimit limit = suggestionLimitRepository.findByUserAndMonthYear(user, currentMonth)
                .orElse(new SuggestionLimit(user, currentMonth));
            
            limit.setMaxSuggestions(newLimit);
            suggestionLimitRepository.save(limit);
            updatedCount++;
        }
        
        return updatedCount;
    }

    @Transactional
    public void cleanupOldLimitRecords(int monthsToKeep) {
        YearMonth cutoff = YearMonth.now().minusMonths(monthsToKeep);
        String cutoffString = cutoff.toString();
        suggestionLimitRepository.deleteOldRecords(cutoffString);
    }
}