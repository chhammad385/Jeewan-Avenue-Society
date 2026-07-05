package com.jeewanavenue.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.Due;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.entity.UserCredit;
import com.jeewanavenue.repository.DueRepository;
import com.jeewanavenue.repository.UserRepository;
import com.jeewanavenue.service.DueService;
import com.jeewanavenue.service.FinancialSchedulerService;
import com.jeewanavenue.service.UserCreditService;
import com.jeewanavenue.service.UserService;

@RestController
@RequestMapping("/api/dues")
public class DueController {
    @Autowired
    private DueService dueService;
    @Autowired
    private DueRepository dueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCreditService userCreditService;
    @Autowired
    private FinancialSchedulerService financialSchedulerService;

    // Set monthly payment for all users
    @PostMapping("/set-monthly")
    public ResponseEntity<String> setMonthlyDue(@RequestBody SetMonthlyDueRequest req) {
        try {
            // Validate request
            if (req.getRatePerMarla() == null || req.getRatePerMarla().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Invalid rate per marla. Must be greater than 0.");
            }
            if (req.getMonth() == null || req.getMonth() < 1 || req.getMonth() > 12) {
                return ResponseEntity.badRequest().body("Invalid month. Must be between 1 and 12.");
            }
            if (req.getYear() == null || req.getYear() < 2020) {
                return ResponseEntity.badRequest().body("Invalid year. Must be 2020 or later.");
            }
            if (req.getIssueDate() == null) {
                req.setIssueDate(LocalDate.now());
            }
            if (req.getGapDays() == null) {
                req.setGapDays(30); // Default 30 days
            }
            if (req.getLateCharges() == null) {
                req.setLateCharges(BigDecimal.valueOf(100)); // Default PKR 100
            }

            List<User> users = userRepository.findAll();
            int processedUsers = 0;
            int skippedUsers = 0;
            int skippedNoPlot = 0;
            int skippedNoPlotSize = 0;
            int skippedInvalidPlotSize = 0;
            int usersWithCreditApplied = 0;
            int updatedExistingDues = 0;
            int newDuesCreated = 0;
            BigDecimal totalCreditUsed = BigDecimal.ZERO;
            StringBuilder skippedDetails = new StringBuilder();
            
            for (User user : users) {
                try {
                    if (user.getPlotNo() == null || user.getPlotNo().trim().isEmpty()) {
                        skippedUsers++;
                        skippedNoPlot++;
                        skippedDetails.append("❌ ").append(user.getOwnerName()).append(" - No plot number\n");
                        continue;
                    }
                    
                    // Get area from user's plotSizeMarla field (from User President Console)
                    BigDecimal area;
                    if (user.getPlotSizeMarla() != null && !user.getPlotSizeMarla().trim().isEmpty()) {
                        try {
                            area = new BigDecimal(user.getPlotSizeMarla().trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid plot size for user " + user.getOwnerName() + ": " + user.getPlotSizeMarla());
                            skippedUsers++;
                            skippedInvalidPlotSize++;
                            skippedDetails.append("❌ ").append(user.getOwnerName()).append(" (Plot: ").append(user.getPlotNo()).append(") - Invalid plot size: ").append(user.getPlotSizeMarla()).append("\n");
                            continue;
                        }
                    } else {
                        System.err.println("No plot size found for user " + user.getOwnerName() + " (Plot: " + user.getPlotNo() + ")");
                        skippedUsers++;
                        skippedNoPlotSize++;
                        skippedDetails.append("❌ ").append(user.getOwnerName()).append(" (Plot: ").append(user.getPlotNo()).append(") - No plot size data\n");
                        continue;
                    }
                    
                    // Skip users with zero or negative area
                    if (area.compareTo(BigDecimal.ZERO) <= 0) {
                        System.err.println("Invalid area (<=0) for user " + user.getOwnerName() + ": " + area);
                        skippedUsers++;
                        skippedInvalidPlotSize++;
                        skippedDetails.append("❌ ").append(user.getOwnerName()).append(" (Plot: ").append(user.getPlotNo()).append(") - Invalid area: ").append(area).append(" marla\n");
                        continue;
                    }
                    
                    BigDecimal dueAmount = req.getRatePerMarla().multiply(area);
                    
                    // Check if due already exists for this user, month, and year
                    Due existingDue = dueRepository.findByUserIdAndMonthAndYear(user.getId(), req.getMonth(), req.getYear());
                    
                    // Apply existing credits automatically
                    BigDecimal existingCredit = userCreditService.getTotalCreditByPlotNo(user.getPlotNo());
                    BigDecimal finalDueAmount = dueAmount;
                    BigDecimal creditUsed = BigDecimal.ZERO;
                    
                    if (existingCredit.compareTo(BigDecimal.ZERO) > 0) {
                        if (existingCredit.compareTo(dueAmount) >= 0) {
                            // Credit is enough to cover full due amount
                            creditUsed = userCreditService.useCredit(user.getPlotNo(), dueAmount, 
                                "Auto-applied to monthly due - " + getMonthName(req.getMonth()) + " " + req.getYear());
                            finalDueAmount = BigDecimal.ZERO;
                        } else {
                            // Partial credit application
                            creditUsed = userCreditService.useCredit(user.getPlotNo(), existingCredit, 
                                "Auto-applied to monthly due - " + getMonthName(req.getMonth()) + " " + req.getYear());
                            finalDueAmount = dueAmount.subtract(creditUsed);
                        }
                        
                        if (creditUsed.compareTo(BigDecimal.ZERO) > 0) {
                            usersWithCreditApplied++;
                            totalCreditUsed = totalCreditUsed.add(creditUsed);
                        }
                    }
                    
                    Due due;
                    if (existingDue != null) {
                        // Update existing due record
                        due = existingDue;
                        due.setAreaMarla(area);
                        due.setDueAmount(finalDueAmount);
                        due.setIssueDate(req.getIssueDate());
                        due.setGapDays(req.getGapDays());
                        due.setLateCharges(req.getLateCharges());
                        updatedExistingDues++;
                    } else {
                        // Create new due record
                        due = new Due();
                        due.setUserId(user.getId());
                        due.setUserName(user.getOwnerName());
                        due.setPlotNo(user.getPlotNo());
                        due.setAreaMarla(area);
                        due.setDueAmount(finalDueAmount);
                        due.setMonth(req.getMonth());
                        due.setYear(req.getYear());
                        due.setIssueDate(req.getIssueDate());
                        due.setGapDays(req.getGapDays());
                        due.setLateCharges(req.getLateCharges());
                        due.setLateChargesApplied(false); // Initialize as false for new dues
                        newDuesCreated++;
                    }
                    
                    dueService.save(due);
                    processedUsers++;
                    
                } catch (Exception e) {
                    System.err.println("Error processing user " + user.getOwnerName() + ": " + e.getMessage());
                    skippedUsers++;
                    skippedDetails.append("❌ ").append(user.getOwnerName()).append(" - Processing error: ").append(e.getMessage()).append("\n");
                }
            }
            
            String result = "Monthly dues set successfully with automatic credit application!\n";
            result += "📊 Total Users in System: " + users.size() + "\n";
            result += "✅ Processed: " + processedUsers + " users\n";
            if (newDuesCreated > 0) {
                result += "📄 New Dues Created: " + newDuesCreated + " users\n";
            }
            if (updatedExistingDues > 0) {
                result += "🔄 Updated Existing Dues: " + updatedExistingDues + " users\n";
            }
            if (usersWithCreditApplied > 0) {
                result += "💰 Credits Applied: " + usersWithCreditApplied + " users (Total: PKR " + totalCreditUsed.toPlainString() + ")\n";
            }
            if (skippedUsers > 0) {
                result += "⚠️ Skipped: " + skippedUsers + " users\n";
                if (skippedNoPlot > 0) {
                    result += "   • " + skippedNoPlot + " users without plot numbers\n";
                }
                if (skippedNoPlotSize > 0) {
                    result += "   • " + skippedNoPlotSize + " users without plot size data\n";
                }
                if (skippedInvalidPlotSize > 0) {
                    result += "   • " + skippedInvalidPlotSize + " users with invalid plot size\n";
                }
                result += "\nSkipped Users Details:\n" + skippedDetails.toString();
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("Error in setMonthlyDue: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to set monthly dues: " + e.getMessage());
        }
    }

    // Get dues with optional filters
    @GetMapping
    public List<Due> getDues(@RequestParam(required = false) Integer month,
                             @RequestParam(required = false) Integer year,
                             @RequestParam(required = false) String userName) {
        if (month != null && year != null && userName != null && !userName.isEmpty()) {
            return dueService.getByMonthYearAndUserName(month, year, userName);
        } else if (month != null && year != null) {
            return dueService.getByMonthAndYear(month, year);
        } else if (userName != null && !userName.isEmpty()) {
            return dueService.getByUserName(userName);
        } else {
            return dueService.getAll();
        }
    }

    // Get current user's total due amount using unified account balance
    @GetMapping("/my-total-due")
    public ResponseEntity<BigDecimal> getMyTotalDue(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BigDecimal.ZERO);
        }
        
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail).orElse(null);
        
        if (currentUser == null) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
        
        // Return the unified account balance directly
        // Positive = due amount, Negative = debit (advance payment)
        BigDecimal accountBalance = currentUser.getAccountBalance();
        return ResponseEntity.ok(accountBalance);
    }

    // Get detailed due information including calculation breakdown
    @GetMapping("/{id}/details")
    public ResponseEntity<DueDetailResponse> getDueDetails(@PathVariable Long id) {
        try {
            Optional<Due> dueOpt = dueService.findById(id);
            if (!dueOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            
            Due due = dueOpt.get();
            
            // Get user information
            User user = userRepository.findById(due.getUserId()).orElse(null);
            
            // Get ALL dues for this user (payment history)
            List<Due> userPaymentHistory = dueRepository.findByUserId(due.getUserId());
            
            // Get ALL credit history for this user
            List<UserCredit> allCreditHistory = userCreditService.getCreditsByUserId(due.getUserId());
            
            // Calculate due date
            LocalDate dueDate = due.getIssueDate().plusDays(due.getGapDays());
            
            // Check if overdue
            boolean isOverdue = LocalDate.now().isAfter(dueDate);
            
            // Find any credit transactions related to this month/year
            String monthYearDesc = getMonthName(due.getMonth()) + " " + due.getYear();
            List<UserCredit> relatedCredits = allCreditHistory.stream()
                .filter(credit -> credit.getDescription() != null && 
                       credit.getDescription().contains(monthYearDesc) &&
                       credit.getCreditAmount().compareTo(BigDecimal.ZERO) < 0) // Negative amounts are usage
                .collect(Collectors.toList());
            
            BigDecimal totalCreditsApplied = relatedCredits.stream()
                .map(UserCredit::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs(); // Make positive for display
            
            // Build response
            DueDetailResponse response = new DueDetailResponse();
            response.setDue(due);
            response.setUser(user);
            response.setUserPaymentHistory(userPaymentHistory);
            response.setAllCreditHistory(allCreditHistory);
            response.setDueDate(dueDate);
            response.setOverdue(isOverdue);
            response.setDaysOverdue(isOverdue ? (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now()) : 0);
            response.setOriginalAmount(due.getDueAmount().add(totalCreditsApplied));
            response.setCreditsApplied(totalCreditsApplied);
            response.setFinalAmount(due.getDueAmount());
            response.setRelatedCreditTransactions(relatedCredits);
            response.setCurrentCreditBalance(userCreditService.getTotalCreditByPlotNo(due.getPlotNo()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Delete a specific due record
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDue(@PathVariable Long id) {
        try {
            // Check if the due record exists
            Optional<Due> dueOpt = dueService.findById(id);
            if (!dueOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Due record not found with ID: " + id);
            }
            
            Due existingDue = dueOpt.get();
            
            // Store details for response message
            String userName = existingDue.getUserName();
            String month = getMonthName(existingDue.getMonth());
            Integer year = existingDue.getYear();
            BigDecimal amount = existingDue.getDueAmount();
            
            // Delete the due record
            dueService.deleteById(id);
            
            String successMessage = String.format(
                "Due record deleted successfully!\n\nDeleted Details:\n- User: %s\n- Month: %s %d\n- Amount: PKR %.2f",
                userName, month, year, amount
            );
            
            return ResponseEntity.ok(successMessage);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting due record: " + e.getMessage());
        }
    }
    
    // Manual late charges check endpoint
    @PostMapping("/apply-late-charges")
    public ResponseEntity<String> applyLateCharges() {
        try {
            String result = financialSchedulerService.manualLateChargesCheck();
            return ResponseEntity.ok("✅ " + result + "\n\n" +
                "📅 Late charges have been applied to overdue payments.\n" +
                "💰 Each due gets late charges applied only ONCE (not daily).\n" +
                "🔄 Future runs will skip dues that already have late charges applied.\n" +
                "📊 Check individual user due amounts for updates.");
        } catch (Exception e) {
            System.err.println("Error in manual late charges: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Error applying late charges: " + e.getMessage());
        }
    }
    
    // Helper method to get month name
    private String getMonthName(Integer monthNum) {
        String[] months = {"", "January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        if (monthNum != null && monthNum >= 1 && monthNum <= 12) {
            return months[monthNum];
        }
        return "Unknown";
    }

    // DTO for set monthly due
    public static class SetMonthlyDueRequest {
        private BigDecimal ratePerMarla;
        private Integer month;
        private Integer year;
        private LocalDate issueDate;
        private Integer gapDays;
        private BigDecimal lateCharges;
        // Getters and setters
        public BigDecimal getRatePerMarla() { return ratePerMarla; }
        public void setRatePerMarla(BigDecimal ratePerMarla) { this.ratePerMarla = ratePerMarla; }
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public LocalDate getIssueDate() { return issueDate; }
        public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
        public Integer getGapDays() { return gapDays; }
        public void setGapDays(Integer gapDays) { this.gapDays = gapDays; }
        public BigDecimal getLateCharges() { return lateCharges; }
        public void setLateCharges(BigDecimal lateCharges) { this.lateCharges = lateCharges; }
    }

    // Response class for detailed due information
    public static class DueDetailResponse {
        private Due due;
        private User user;
        private List<Due> userPaymentHistory;
        private List<UserCredit> allCreditHistory;
        private LocalDate dueDate;
        private boolean isOverdue;
        private int daysOverdue;
        private BigDecimal originalAmount;
        private BigDecimal creditsApplied;
        private BigDecimal finalAmount;
        private List<UserCredit> relatedCreditTransactions;
        private BigDecimal currentCreditBalance;

        // Getters and setters
        public Due getDue() { return due; }
        public void setDue(Due due) { this.due = due; }
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        public List<Due> getUserPaymentHistory() { return userPaymentHistory; }
        public void setUserPaymentHistory(List<Due> userPaymentHistory) { this.userPaymentHistory = userPaymentHistory; }
        public List<UserCredit> getAllCreditHistory() { return allCreditHistory; }
        public void setAllCreditHistory(List<UserCredit> allCreditHistory) { this.allCreditHistory = allCreditHistory; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public boolean isOverdue() { return isOverdue; }
        public void setOverdue(boolean overdue) { isOverdue = overdue; }
        public int getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
        public BigDecimal getCreditsApplied() { return creditsApplied; }
        public void setCreditsApplied(BigDecimal creditsApplied) { this.creditsApplied = creditsApplied; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
        public List<UserCredit> getRelatedCreditTransactions() { return relatedCreditTransactions; }
        public void setRelatedCreditTransactions(List<UserCredit> relatedCreditTransactions) { this.relatedCreditTransactions = relatedCreditTransactions; }
        public BigDecimal getCurrentCreditBalance() { return currentCreditBalance; }
        public void setCurrentCreditBalance(BigDecimal currentCreditBalance) { this.currentCreditBalance = currentCreditBalance; }
    }
}
