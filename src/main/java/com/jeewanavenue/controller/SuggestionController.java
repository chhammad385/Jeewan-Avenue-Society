package com.jeewanavenue.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.Suggestion;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;
import com.jeewanavenue.service.SuggestionService;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private UserRepository userRepository;

    // --- PUBLIC ENDPOINTS FOR SUGGESTION SUBMISSION ---

    /**
     * Submit a new suggestion
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitSuggestion(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = authentication.getName();
            String title = request.get("title");
            String description = request.get("description");
            String category = request.get("category");
            String priority = request.get("priority");

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (description == null || description.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Description is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (category == null || category.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Category is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (priority == null || priority.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Priority is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Submit suggestion
            Suggestion suggestion = suggestionService.submitSuggestion(
                userEmail, 
                title.trim(), 
                description.trim(), 
                category.trim(), 
                priority.trim()
            );

            response.put("success", true);
            response.put("message", "Suggestion submitted successfully");
            response.put("suggestionId", suggestion.getId());
            
            // Include updated limit info
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                Map<String, Object> limitInfo = suggestionService.getUserMonthlyLimitInfo(userOpt.get());
                response.put("limitInfo", limitInfo);
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while submitting your suggestion");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user's monthly limit information
     */
    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getUserLimits(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Map<String, Object> limitInfo = suggestionService.getUserMonthlyLimitInfo(userOpt.get());
            return ResponseEntity.ok(limitInfo);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unable to fetch limit information");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get user's own suggestions
     */
    @GetMapping("/my-suggestions")
    public ResponseEntity<List<Suggestion>> getUserSuggestions(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<Suggestion> suggestions = suggestionService.findSuggestionsByUser(userOpt.get());
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- ADMIN ENDPOINTS FOR SUGGESTION MANAGEMENT ---

    /**
     * Get all suggestions (Admin only)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Suggestion>> getAllSuggestions(Authentication authentication) {
        try {
            // First, let's check if we're receiving any suggestions at all
            List<Suggestion> allSuggestions = suggestionService.findAllSuggestions();
            System.out.println("DEBUG: Total suggestions in database: " + allSuggestions.size());
            
            if (authentication == null) {
                System.out.println("DEBUG: No authentication provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check if user has admin privileges (you can customize this based on your role system)
            String userEmail = authentication.getName();
            System.out.println("DEBUG: User email: " + userEmail);
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent()) {
                System.out.println("DEBUG: User not found in database");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = userOpt.get();
            System.out.println("DEBUG: User role: " + user.getRole());
            // Check if user has admin role (adjust based on your role field)
            if (!isAdminUser(user)) {
                System.out.println("DEBUG: User is not admin");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            System.out.println("DEBUG: Returning " + allSuggestions.size() + " suggestions to admin");
            return ResponseEntity.ok(allSuggestions);

        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getAllSuggestions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Test endpoint to check suggestions count (temporary for debugging)
     */
    @GetMapping("/test-count")
    public ResponseEntity<Map<String, Object>> getTestCount() {
        try {
            List<Suggestion> allSuggestions = suggestionService.findAllSuggestions();
            Map<String, Object> response = new HashMap<>();
            response.put("totalSuggestions", allSuggestions.size());
            response.put("suggestions", allSuggestions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get unread suggestions count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            long unreadCount = suggestionService.getUnreadCount();
            Map<String, Object> response = new HashMap<>();
            response.put("count", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unable to fetch unread count");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get unread suggestions (Admin only)
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Suggestion>> getUnreadSuggestions(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent() || !isAdminUser(userOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Suggestion> suggestions = suggestionService.findUnreadSuggestions();
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark suggestion as read (Admin only)
     */
    @PatchMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id, 
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent() || !isAdminUser(userOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            suggestionService.markAsRead(id);
            
            response.put("success", true);
            response.put("message", "Suggestion marked as read");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking suggestion as read");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mark suggestion as unread (Admin only)
     */
    @PatchMapping("/{id}/mark-unread")
    public ResponseEntity<Map<String, Object>> markAsUnread(
            @PathVariable Long id, 
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent() || !isAdminUser(userOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            suggestionService.markAsUnread(id);
            
            response.put("success", true);
            response.put("message", "Suggestion marked as unread");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking suggestion as unread");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get suggestions with filters (Admin only)
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Suggestion>> getSuggestionsWithFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            Authentication authentication) {
        
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent() || !isAdminUser(userOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Suggestion> suggestions = suggestionService.findSuggestionsWithFilters(
                category, priority, isRead, sortBy);
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get suggestion statistics (Admin only)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSuggestionStatistics(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (!userOpt.isPresent() || !isAdminUser(userOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Map<String, Object> stats = suggestionService.getSuggestionStatistics();
            Map<String, Object> limitStats = suggestionService.getMonthlyLimitStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("suggestions", stats);
            response.put("limits", limitStats);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unable to fetch statistics");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update user monthly limit (Admin only)
     */
    @PutMapping("/limits/{userEmail}")
    public ResponseEntity<Map<String, Object>> updateUserLimit(
            @PathVariable String userEmail,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String adminEmail = authentication.getName();
            Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
            
            if (!adminOpt.isPresent() || !isAdminUser(adminOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Integer newLimit = request.get("maxSuggestions");
            if (newLimit == null || newLimit < 0) {
                response.put("success", false);
                response.put("message", "Invalid limit value");
                return ResponseEntity.badRequest().body(response);
            }

            suggestionService.updateUserMonthlyLimit(userEmail, newLimit);
            
            response.put("success", true);
            response.put("message", "User limit updated successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user limit");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mark all unread suggestions as read (ADMIN ONLY)
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userOpt.get();
            if (!isAdminUser(user)) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            int updatedCount = suggestionService.markAllSuggestionsAsRead();
            
            response.put("success", true);
            response.put("message", "All suggestions marked as read");
            response.put("updatedCount", updatedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking all suggestions as read");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user limits for management (ADMIN ONLY)
     */
    @GetMapping("/user-limits")
    public ResponseEntity<List<Map<String, Object>>> getAllUserLimits(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userOpt.get();
            if (!isAdminUser(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Map<String, Object>> userLimits = suggestionService.getAllUserLimitsInfo();
            return ResponseEntity.ok(userLimits);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update specific user's suggestion limit (ADMIN ONLY)
     */
    @PutMapping("/user-limits/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserLimit(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String adminEmail = authentication.getName();
            Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
            
            if (adminOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User admin = adminOpt.get();
            if (!isAdminUser(admin)) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Get target user
            Optional<User> targetUserOpt = userRepository.findById(userId);
            if (targetUserOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Target user not found");
                return ResponseEntity.badRequest().body(response);
            }

            User targetUser = targetUserOpt.get();
            Integer newLimit = (Integer) request.get("maxSuggestions");
            
            if (newLimit == null || newLimit < 0) {
                response.put("success", false);
                response.put("message", "Invalid limit value");
                return ResponseEntity.badRequest().body(response);
            }

            suggestionService.updateUserMonthlyLimit(targetUser.getEmail(), newLimit);
            
            response.put("success", true);
            response.put("message", "User limit updated successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating user limit");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update suggestion limits for all users (ADMIN ONLY)
     */
    @PutMapping("/user-limits/bulk")
    public ResponseEntity<Map<String, Object>> updateAllUserLimits(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String adminEmail = authentication.getName();
            Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
            
            if (adminOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User admin = adminOpt.get();
            if (!isAdminUser(admin)) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Integer newLimit = (Integer) request.get("maxSuggestions");
            
            if (newLimit == null || newLimit < 0) {
                response.put("success", false);
                response.put("message", "Invalid limit value");
                return ResponseEntity.badRequest().body(response);
            }

            int updatedCount = suggestionService.updateAllUsersMonthlyLimit(newLimit);
            
            response.put("success", true);
            response.put("message", "All user limits updated successfully");
            response.put("updatedCount", updatedCount);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating all user limits");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- HELPER METHODS ---

    /**
     * Check if user has admin privileges
     * All position holders except Society-Member can access suggestion management
     * 
     * Allowed Roles:
     * - President, Vice-President
     * - General-Secretary, Joint-Secretary, Additional-Secretary, Assistant-Secretary, Information-Secretary
     * - Finance-Secretary, Treasurer, Joint-Treasurer  
     * - Security-Officer, Maintenance-Officer
     * - Sports-Secretary, Cultural-Secretary, Welfare-Secretary
     * - Legal-Advisor, Committee-Member
     * 
     * Excluded Role:
     * - Society-Member (regular residents without position)
     */
    private boolean isAdminUser(User user) {
        String role = user.getRole();
        return role != null && !role.equals("Society-Member") && (
            role.equals("President") || 
            role.equals("Vice-President") ||
            role.equals("General-Secretary") || 
            role.equals("Joint-Secretary") ||
            role.equals("Additional-Secretary") ||
            role.equals("Assistant-Secretary") ||
            role.equals("Information-Secretary") ||
            role.equals("Finance-Secretary") ||
            role.equals("Treasurer") ||
            role.equals("Joint-Treasurer") ||
            role.equals("Security-Officer") ||
            role.equals("Maintenance-Officer") ||
            role.equals("Sports-Secretary") ||
            role.equals("Cultural-Secretary") ||
            role.equals("Welfare-Secretary") ||
            role.equals("Legal-Advisor") ||
            role.equals("Committee-Member")
        );
    }
}