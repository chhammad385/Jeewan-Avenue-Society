package com.jeewanavenue.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.Feedback;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;
import com.jeewanavenue.service.FeedbackService;
import com.jeewanavenue.service.StaffService;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffService staffService;

    @GetMapping
    public List<Map<String, Object>> getAllFeedback() {
        List<Feedback> feedbacks = feedbackService.findAll();
        
        // Enrich feedback data with plot number
        return feedbacks.stream().map(feedback -> {
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("id", feedback.getId());
            feedbackData.put("category", feedback.getCategory());
            feedbackData.put("personName", feedback.getPersonName());
            feedbackData.put("isSatisfied", feedback.getIsSatisfied());
            feedbackData.put("comments", feedback.getComments());
            feedbackData.put("submittedByUserId", feedback.getSubmittedByUserId());
            feedbackData.put("submittedAt", feedback.getSubmittedAt());
            feedbackData.put("timeZone", feedback.getTimeZone());
            
            // Add plot number if user exists
            if (feedback.getSubmittedByUserId() != null) {
                Optional<User> userOpt = userRepository.findById(feedback.getSubmittedByUserId());
                if (userOpt.isPresent()) {
                    feedbackData.put("plotNo", userOpt.get().getPlotNo());
                } else {
                    feedbackData.put("plotNo", null);
                }
            } else {
                feedbackData.put("plotNo", null);
            }
            
            return feedbackData;
        }).collect(Collectors.toList());
    }

    /**
     * Checks if the current user needs to submit monthly feedback
     */
    @GetMapping("/check-required")
    public ResponseEntity<Map<String, Object>> checkFeedbackRequired(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        
        // **NEW LOGIC: Check if any staff members exist**
        List<?> allStaff = staffService.findAll();
        if (allStaff.isEmpty()) {
            // No staff members exist, so feedback is not required
            Map<String, Object> response = new HashMap<>();
            response.put("isRequired", false);
            response.put("message", "No staff members are currently available. Feedback is optional.");
            response.put("lastFeedbackDate", user.getLastFeedbackDate());
            response.put("noStaffMembers", true);
            return ResponseEntity.ok(response);
        }
        
        // **ENHANCED LOGIC: If staff members exist now, check if user's last access was "no staff"**
        if (user.getLastAccessWasNoStaff() != null && user.getLastAccessWasNoStaff()) {
            // User previously got access via "no staff" bypass, but now staff exists
            // So feedback is required regardless of the date
            Map<String, Object> response = new HashMap<>();
            response.put("isRequired", true);
            response.put("message", "Staff members are now available. Please provide your feedback to continue using the portal.");
            response.put("lastFeedbackDate", user.getLastFeedbackDate());
            response.put("staffNowAvailable", true);
            response.put("noStaffMembers", false);
            return ResponseEntity.ok(response);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate lastFeedbackDate = user.getLastFeedbackDate();
        
        boolean isRequired = false;
        String message = "";
        
        if (lastFeedbackDate == null) {
            // User has never submitted feedback
            isRequired = true;
            message = "You must complete the monthly feedback form to access the portal.";
        } else {
            // Check if it's been more than 30 days since last feedback
            LocalDate thirtyDaysAgo = today.minusDays(30);
            if (lastFeedbackDate.isBefore(thirtyDaysAgo)) {
                isRequired = true;
                message = "Your monthly feedback is due. Please complete it to continue using the portal.";
            } else {
                message = "Thank you! Your feedback is up to date.";
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isRequired", isRequired);
        response.put("message", message);
        response.put("lastFeedbackDate", lastFeedbackDate);
        response.put("daysSinceLastFeedback", lastFeedbackDate != null ? 
            java.time.temporal.ChronoUnit.DAYS.between(lastFeedbackDate, today) : null);
        response.put("noStaffMembers", false);

        return ResponseEntity.ok(response);
    }

    /**
     * Marks feedback as completed when no staff members are present
     * This allows users to access the portal without submitting actual feedback
     */
    @PostMapping("/complete-no-staff")
    public ResponseEntity<Map<String, Object>> completeNoStaffFeedback(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        
        // Verify that no staff members exist before allowing this action
        List<?> allStaff = staffService.findAll();
        if (!allStaff.isEmpty()) {
            // Staff members exist, so this endpoint should not be used
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Staff members are present. Please complete the normal feedback process.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Update the user's last feedback date to current date
        // This grants them portal access as if they completed feedback
        // BUT mark that this was a "no staff" access, not real feedback
        user.setLastFeedbackDate(LocalDate.now());
        user.setLastAccessWasNoStaff(true);
        userRepository.save(user);
        
        System.out.println("Granted portal access to user: " + userEmail + " (no staff members present)");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Portal access granted. No feedback required when no staff members are present.");
        response.put("lastFeedbackDate", LocalDate.now());
        response.put("noStaffMembers", true);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody List<Feedback> feedbackList, Authentication authentication) {
        try {
            System.out.println("Received feedback list with " + feedbackList.size() + " items");
            
            // Get the current user to update their last feedback date
            final User currentUser;
            if (authentication != null) {
                String userEmail = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(userEmail);
                currentUser = userOpt.orElse(null);
            } else {
                currentUser = null;
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required to submit feedback");
            }
            
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            }
            
            // **MONTHLY RESTRICTION: Check if user has already submitted feedback this month**
            LocalDate today = LocalDate.now();
            LocalDate lastFeedbackDate = currentUser.getLastFeedbackDate();
            
            if (lastFeedbackDate != null) {
                LocalDate thirtyDaysAgo = today.minusDays(30);
                if (lastFeedbackDate.isAfter(thirtyDaysAgo)) {
                    long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastFeedbackDate, today);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("You have already submitted feedback this month. Please wait " + 
                              (30 - daysSince) + " more day(s) before submitting again.");
                }
            }
            
            // Process feedback submissions
            List<Feedback> savedFeedbackList = feedbackList.stream()
                    .map(feedback -> {
                        // Set the user ID for tracking
                        feedback.setSubmittedByUserId(currentUser.getId());
                        
                        // **IMPROVED TIMESTAMP HANDLING**: Use client timestamp if provided and valid
                        if (feedback.getSubmittedAt() == null) {
                            feedback.setSubmittedAt(java.time.LocalDateTime.now());
                        } else {
                            // Validate that the client timestamp is reasonable (not too far in future/past)
                            java.time.LocalDateTime clientTime = feedback.getSubmittedAt();
                            java.time.LocalDateTime serverTime = java.time.LocalDateTime.now();
                            long hoursDiff = java.time.temporal.ChronoUnit.HOURS.between(clientTime, serverTime);
                            
                            // If client time is more than 24 hours off, use server time instead
                            if (Math.abs(hoursDiff) > 24) {
                                System.out.println("Client timestamp too far off, using server time instead. " +
                                                 "Client: " + clientTime + ", Server: " + serverTime);
                                feedback.setSubmittedAt(serverTime);
                            }
                        }
                        
                        // Use client timezone if provided, otherwise default to Asia/Karachi
                        if (feedback.getTimeZone() == null || feedback.getTimeZone().isEmpty()) {
                            feedback.setTimeZone("Asia/Karachi"); // Default timezone for Pakistan
                        }
                        
                        System.out.println("Saving feedback for: " + feedback.getPersonName() + 
                                         ", Category: " + feedback.getCategory() + 
                                         ", Satisfied: " + feedback.getIsSatisfied() +
                                         ", Submitted at: " + feedback.getSubmittedAt() +
                                         ", Timezone: " + feedback.getTimeZone());
                        return feedbackService.save(feedback);
                    })
                    .toList();
            
            // Update the user's last feedback date
            currentUser.setLastFeedbackDate(today);
            currentUser.setLastAccessWasNoStaff(false); // Clear the no-staff flag since real feedback was provided
            userRepository.save(currentUser);
            System.out.println("Updated last feedback date for user: " + currentUser.getEmail() + " to: " + today);
                    
            System.out.println("Successfully saved " + savedFeedbackList.size() + " feedback items");
            
            // Return success response with feedback details
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feedback submitted successfully");
            response.put("feedbackCount", savedFeedbackList.size());
            response.put("submissionDate", today);
            response.put("nextFeedbackDue", today.plusDays(30));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.err.println("Error saving feedback: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/single")
    public ResponseEntity<Feedback> submitSingleFeedback(@RequestBody Feedback feedback) {
        // Use client-provided timestamp and timezone if available, otherwise set current
        if (feedback.getSubmittedAt() == null) {
            feedback.setSubmittedAt(java.time.LocalDateTime.now());
        }
        // Use client timezone if provided, otherwise default to Asia/Karachi
        if (feedback.getTimeZone() == null || feedback.getTimeZone().isEmpty()) {
            feedback.setTimeZone("Asia/Karachi"); // Default timezone
        }
        
        System.out.println("Saving single feedback - Timezone: " + feedback.getTimeZone());
        
        // For single feedback submissions
        Feedback savedFeedback = feedbackService.save(feedback);
        return new ResponseEntity<>(savedFeedback, HttpStatus.CREATED);
    }
}