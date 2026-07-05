package com.jeewanavenue.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if user can access portal features (feedback must be completed within last 30 days)
     */
    @GetMapping("/check-portal-access")
    public ResponseEntity<Map<String, Object>> checkPortalAccess(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        LocalDate today = LocalDate.now();
        LocalDate lastFeedbackDate = user.getLastFeedbackDate();
        
        boolean canAccess = false;
        String reason = "";
        
        if (lastFeedbackDate == null) {
            reason = "FEEDBACK_REQUIRED_NEVER_SUBMITTED";
        } else {
            LocalDate thirtyDaysAgo = today.minusDays(30);
            if (lastFeedbackDate.isBefore(thirtyDaysAgo)) {
                reason = "FEEDBACK_REQUIRED_EXPIRED";
            } else {
                canAccess = true;
                reason = "ACCESS_GRANTED";
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("canAccess", canAccess);
        response.put("reason", reason);
        response.put("lastFeedbackDate", lastFeedbackDate);
        response.put("daysSinceLastFeedback", lastFeedbackDate != null ? 
            java.time.temporal.ChronoUnit.DAYS.between(lastFeedbackDate, today) : null);

        return ResponseEntity.ok(response);
    }
}
