package com.jeewanavenue.controller;

import com.jeewanavenue.entity.CustomDue;
import com.jeewanavenue.service.CustomDueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/custom-dues")
public class CustomDueController {

    @Autowired
    private CustomDueService customDueService;

    /**
     * Create a new custom due
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomDue(
            @RequestBody CreateCustomDueRequest request,
            Authentication authentication) {
        try {
            String createdBy = authentication.getName();
            
            // Parse due date if provided
            LocalDateTime dueDate = null;
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                dueDate = LocalDateTime.parse(request.getDueDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            CustomDue customDue = customDueService.createCustomDue(
                request.getUserId(),
                request.getAmount(),
                request.getReason(),
                request.getCategory(),
                createdBy,
                dueDate,
                request.getNotes()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Custom due created successfully",
                "customDue", customDue
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }

    /**
     * Get all custom dues for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CustomDue>> getCustomDuesByUserId(@PathVariable Long userId) {
        List<CustomDue> customDues = customDueService.getCustomDuesByUserId(userId);
        return ResponseEntity.ok(customDues);
    }

    /**
     * Get all unpaid custom dues for a specific user
     */
    @GetMapping("/user/{userId}/unpaid")
    public ResponseEntity<List<CustomDue>> getUnpaidCustomDuesByUserId(@PathVariable Long userId) {
        List<CustomDue> customDues = customDueService.getUnpaidCustomDuesByUserId(userId);
        return ResponseEntity.ok(customDues);
    }

    /**
     * Get total unpaid custom due amount for a user
     */
    @GetMapping("/user/{userId}/total-unpaid")
    public ResponseEntity<Map<String, Object>> getTotalUnpaidAmount(@PathVariable Long userId) {
        BigDecimal totalAmount = customDueService.getTotalUnpaidAmountByUserId(userId);
        Long count = customDueService.countUnpaidCustomDuesByUserId(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "totalUnpaidAmount", totalAmount,
            "unpaidCount", count
        ));
    }

    /**
     * Mark a custom due as paid
     */
    @PutMapping("/{customDueId}/mark-paid")
    public ResponseEntity<Map<String, Object>> markCustomDueAsPaid(
            @PathVariable Long customDueId,
            Authentication authentication) {
        try {
            String paidBy = authentication.getName();
            CustomDue customDue = customDueService.markAsPaid(customDueId, paidBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Custom due marked as paid successfully",
                "customDue", customDue
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }

    /**
     * Update a custom due
     */
    @PutMapping("/{customDueId}")
    public ResponseEntity<Map<String, Object>> updateCustomDue(
            @PathVariable Long customDueId,
            @RequestBody UpdateCustomDueRequest request) {
        try {
            // Parse due date if provided
            LocalDateTime dueDate = null;
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                dueDate = LocalDateTime.parse(request.getDueDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            CustomDue customDue = customDueService.updateCustomDue(
                customDueId,
                request.getAmount(),
                request.getReason(),
                request.getCategory(),
                dueDate,
                request.getNotes()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Custom due updated successfully",
                "customDue", customDue
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }

    /**
     * Delete a custom due
     */
    @DeleteMapping("/{customDueId}")
    public ResponseEntity<Map<String, Object>> deleteCustomDue(@PathVariable Long customDueId) {
        try {
            customDueService.deleteCustomDue(customDueId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Custom due deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", e.getMessage()
                    ));
        }
    }

    /**
     * Get all custom dues (admin only)
     */
    @GetMapping
    public ResponseEntity<List<CustomDue>> getAllCustomDues() {
        List<CustomDue> customDues = customDueService.getAllCustomDues();
        return ResponseEntity.ok(customDues);
    }

    /**
     * Get all unpaid custom dues (admin only)
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<CustomDue>> getAllUnpaidCustomDues() {
        List<CustomDue> customDues = customDueService.getAllUnpaidCustomDues();
        return ResponseEntity.ok(customDues);
    }

    /**
     * Get custom dues by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CustomDue>> getCustomDuesByCategory(@PathVariable String category) {
        List<CustomDue> customDues = customDueService.getCustomDuesByCategory(category);
        return ResponseEntity.ok(customDues);
    }

    /**
     * Get a specific custom due by ID
     */
    @GetMapping("/{customDueId}")
    public ResponseEntity<CustomDue> getCustomDueById(@PathVariable Long customDueId) {
        Optional<CustomDue> customDueOpt = customDueService.getCustomDueById(customDueId);
        if (customDueOpt.isPresent()) {
            return ResponseEntity.ok(customDueOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DTOs for request bodies
    public static class CreateCustomDueRequest {
        private Long userId;
        private BigDecimal amount;
        private String reason;
        private String category;
        private String dueDate;
        private String notes;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class UpdateCustomDueRequest {
        private BigDecimal amount;
        private String reason;
        private String category;
        private String dueDate;
        private String notes;

        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}