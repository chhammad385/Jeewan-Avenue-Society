package com.jeewanavenue.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.CustomDue;
import com.jeewanavenue.entity.Due;
import com.jeewanavenue.entity.MonthlyPayment;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.service.CustomDueService;
import com.jeewanavenue.service.DueService;
import com.jeewanavenue.service.MonthlyPaymentService;
import com.jeewanavenue.service.UserDebitService;
import com.jeewanavenue.service.UserService;

@RestController
@RequestMapping("/api/monthly-payments")
public class MonthlyPaymentController {

    @Autowired
    private MonthlyPaymentService monthlyPaymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DueService dueService;

    @Autowired
    private UserDebitService userDebitService;



    @Autowired
    private CustomDueService customDueService;

    /**
     * Get all monthly payment configurations
     */
    @GetMapping
    public ResponseEntity<List<MonthlyPayment>> getAllMonthlyPayments() {
        List<MonthlyPayment> payments = monthlyPaymentService.getAllMonthlyPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Get monthly payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MonthlyPayment> getMonthlyPaymentById(@PathVariable Long id) {
        Optional<MonthlyPayment> payment = monthlyPaymentService.getMonthlyPaymentById(id);
        return payment.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Set new monthly payment (create dues for selected users)
     */
    @PostMapping("/set")
    public ResponseEntity<?> setMonthlyPayment(@RequestBody Map<String, Object> requestBody, Authentication authentication) {
        try {
            // Get current user
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has Finance Secretary role (handle both formats)
            String userRole = currentUser.getRole();
            boolean isFinanceSecretary = "Finance Secretary".equals(userRole) || 
                                       "Finance-Secretary".equals(userRole) ||
                                       "FINANCE_SECRETARY".equals(userRole);
            
            if (!isFinanceSecretary) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Finance Secretary can set monthly payments. Current role: " + userRole));
            }

            // Extract MonthlyPayment data
            MonthlyPayment monthlyPayment = new MonthlyPayment();
            monthlyPayment.setRatePerMarla(new BigDecimal(requestBody.get("ratePerMarla").toString()));
            monthlyPayment.setPropertyType(requestBody.get("propertyType").toString());
            monthlyPayment.setBuiltStatus(requestBody.get("builtStatus").toString());
            monthlyPayment.setMonth(Integer.parseInt(requestBody.get("month").toString()));
            monthlyPayment.setYear(Integer.parseInt(requestBody.get("year").toString()));
            monthlyPayment.setIssueDate(LocalDate.parse(requestBody.get("issueDate").toString()));
            monthlyPayment.setGapDays(Integer.parseInt(requestBody.get("gapDays").toString()));
            monthlyPayment.setLateCharges(new BigDecimal(requestBody.get("lateCharges").toString()));
            monthlyPayment.setCreatedBy(currentUser.getId());

            // Extract user IDs
            @SuppressWarnings("unchecked")
            List<Integer> userIdsInt = (List<Integer>) requestBody.get("userIds");
            List<Long> userIds = userIdsInt.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());

            MonthlyPayment savedPayment = monthlyPaymentService.setMonthlyPayment(monthlyPayment, userIds, currentUser);
            
            String message = String.format("Monthly payment set successfully for %s %d. Dues generated for %d selected users. Bill messages have been generated and logged in the console.", 
                getMonthName(monthlyPayment.getMonth()), 
                monthlyPayment.getYear(),
                userIds.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "payment", savedPayment,
                "usersProcessed", userIds.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Process single user payment and return WhatsApp message
     * This endpoint processes payment for one user and returns the formatted message
     */
    @PostMapping("/process-single-user")
    public ResponseEntity<?> processSingleUser(@RequestBody Map<String, Object> requestBody, Authentication authentication) {
        try {
            // Get current user
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check Finance Secretary role
            String userRole = currentUser.getRole();
            boolean isFinanceSecretary = "Finance Secretary".equals(userRole) || 
                                       "Finance-Secretary".equals(userRole) ||
                                       "FINANCE_SECRETARY".equals(userRole);
            
            if (!isFinanceSecretary) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Finance Secretary can set monthly payments"));
            }

            // Extract data
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            MonthlyPayment monthlyPayment = new MonthlyPayment();
            monthlyPayment.setRatePerMarla(new BigDecimal(requestBody.get("ratePerMarla").toString()));
            monthlyPayment.setPropertyType(requestBody.get("propertyType").toString());
            monthlyPayment.setBuiltStatus(requestBody.get("builtStatus").toString());
            monthlyPayment.setMonth(Integer.parseInt(requestBody.get("month").toString()));
            monthlyPayment.setYear(Integer.parseInt(requestBody.get("year").toString()));
            monthlyPayment.setIssueDate(LocalDate.parse(requestBody.get("issueDate").toString()));
            monthlyPayment.setGapDays(Integer.parseInt(requestBody.get("gapDays").toString()));
            monthlyPayment.setLateCharges(new BigDecimal(requestBody.get("lateCharges").toString()));

            // Extract Finance Secretary details from request
            String fsName = requestBody.getOrDefault("fsName", "Muhammad Raza").toString();
            String fsPlotNo = requestBody.getOrDefault("fsPlotNo", "H-248").toString();
            String fsPhone = requestBody.getOrDefault("fsPhone", "03006913494").toString();

            // Create Finance Secretary user object for message generation
            User financeSecretary = new User();
            financeSecretary.setOwnerName(fsName);
            financeSecretary.setPlotNo(fsPlotNo);
            financeSecretary.setPhoneNo(fsPhone);

            // Process single user and get message
            Map<String, Object> result = monthlyPaymentService.processSingleUserPayment(
                userId, monthlyPayment, financeSecretary);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage(), "success", false));
        }
    }

    /**
     * Get all users with their account information for the monthly payment page
     */
    @GetMapping("/users-accounts")
    public ResponseEntity<List<UserAccountInfo>> getUsersWithAccounts() {
        List<User> allUsers = userService.findAll();
        
        List<UserAccountInfo> userAccounts = allUsers.stream()
                .filter(user -> user.getPlotNo() != null && !user.getPlotNo().isEmpty())
                .map(user -> {
                    UserAccountInfo info = new UserAccountInfo();
                    info.setUserId(user.getId());
                    info.setUserName(user.getOwnerName());
                    info.setPlotNo(user.getPlotNo());
                    info.setPlotSize(user.getPlotSizeMarla());
                    info.setPropertyType(user.getPropertyType());
                    info.setBuiltStatus(user.getBuiltStatus());
                    info.setEmail(user.getEmail());
                    info.setPhoneNo(user.getPhoneNo());
                    
                    // Use unified account balance approach
                    BigDecimal accountBalance = user.getAccountBalance();
                    
                    if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                        // Positive balance = user has due amount
                        info.setTotalDue(accountBalance);
                        info.setDebitBalance(BigDecimal.ZERO);
                    } else {
                        // Negative balance = user has debit (advance payment)
                        info.setTotalDue(BigDecimal.ZERO);
                        info.setDebitBalance(accountBalance.abs());
                    }
                    
                    return info;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userAccounts);
    }

    /**
     * Get users filtered by month and built status for monthly payment analysis
     */
    @GetMapping("/users-accounts/filtered")
    public ResponseEntity<Map<String, Object>> getUsersAccountsFiltered(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String builtStatus) {
        
        List<User> allUsers = userService.findAll();
        
        // Filter users based on criteria
        List<UserAccountInfo> userAccounts = allUsers.stream()
                .filter(user -> user.getPlotNo() != null && !user.getPlotNo().isEmpty())
                .filter(user -> {
                    // Built status filter
                    if (builtStatus != null && !builtStatus.isEmpty()) {
                        return builtStatus.equals(user.getBuiltStatus());
                    }
                    return true;
                })
                .map(user -> {
                    UserAccountInfo info = new UserAccountInfo();
                    info.setUserId(user.getId());
                    info.setUserName(user.getOwnerName());
                    info.setPlotNo(user.getPlotNo());
                    info.setPlotSize(user.getPlotSizeMarla());
                    info.setPropertyType(user.getPropertyType());
                    info.setBuiltStatus(user.getBuiltStatus());
                    info.setEmail(user.getEmail());
                    info.setPhoneNo(user.getPhoneNo());
                    
                    // Use unified account balance approach
                    BigDecimal accountBalance = user.getAccountBalance();
                    
                    if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                        // Positive balance = user has due amount
                        info.setTotalDue(accountBalance);
                        info.setDebitBalance(BigDecimal.ZERO);
                    } else {
                        // Negative balance = user has debit (advance payment)
                        info.setTotalDue(BigDecimal.ZERO);
                        info.setDebitBalance(accountBalance.abs());
                    }
                    
                    return info;
                })
                .collect(Collectors.toList());
        
        // Get monthly payment info if month/year specified
        MonthlyPayment monthlyPayment = null;
        if (month != null && year != null) {
            Optional<MonthlyPayment> paymentOpt = monthlyPaymentService.getMonthlyPaymentByMonthAndYear(month, year);
            if (paymentOpt.isPresent()) {
                monthlyPayment = paymentOpt.get();
                
                // Further filter by built status if payment has built status restriction
                final String paymentBuiltStatus = monthlyPayment.getBuiltStatus();
                if (paymentBuiltStatus != null && !paymentBuiltStatus.isEmpty()) {
                    userAccounts = userAccounts.stream()
                            .filter(user -> paymentBuiltStatus.equals(user.getBuiltStatus()))
                            .collect(Collectors.toList());
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userAccounts);
        response.put("monthlyPayment", monthlyPayment);
        response.put("totalUsers", userAccounts.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user account details with payment history and debit information
     */
    @GetMapping("/user-details/{userId}")
    public ResponseEntity<UserAccountDetails> getUserAccountDetails(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            UserAccountDetails details = new UserAccountDetails();
            details.setUser(user);
            
            // Get all monthly payments (set by system)
            List<MonthlyPayment> allSetPayments = monthlyPaymentService.getAllMonthlyPayments();
            details.setAllSetMonthlyPayments(allSetPayments);
            
            // Use unified account balance approach
            BigDecimal accountBalance = user.getAccountBalance();
            
            // Get payment history (dues) for reference only - not for calculation
            List<Due> paymentHistory = dueService.getDuesByUserId(userId);
            details.setPaymentHistory(paymentHistory);
            
            // Set debit amount based on account balance
            if (accountBalance.compareTo(BigDecimal.ZERO) < 0) {
                // Negative balance = user has debit (advance payment)
                details.setDebitAmount(accountBalance.abs());
            } else {
                // Positive or zero balance = no debit
                details.setDebitAmount(BigDecimal.ZERO);
            }
            
            // Get custom dues for this user (these are added to account balance when created)
            List<CustomDue> customDues = customDueService.getCustomDuesByUserId(userId);
            details.setCustomDues(customDues);
            
            // Custom due total is now part of account balance, so just show unpaid for reference
            BigDecimal totalCustomDueAmount = customDueService.getTotalUnpaidAmountByUserId(userId);
            details.setTotalCustomDueAmount(totalCustomDueAmount);
            
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get total due amount for all users (for summary display)
     */
    @GetMapping("/total-due")
    public ResponseEntity<Map<String, Object>> getTotalDueAmount() {
        try {
            // Get all users and calculate their net due amounts
            List<User> allUsers = userService.findAll();
            BigDecimal totalNetDue = BigDecimal.ZERO;
            int usersWithDues = 0;
            
            for (User user : allUsers) {
                if (user.getPlotNo() != null && !user.getPlotNo().isEmpty()) {
                    // Use unified account balance approach
                    BigDecimal accountBalance = user.getAccountBalance();
                    
                    if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                        // Positive balance = user has due amount
                        totalNetDue = totalNetDue.add(accountBalance);
                        usersWithDues++;
                    }
                    // Negative balance is debit - not counted in total dues
                }
            }
            
            BigDecimal averageDue = usersWithDues > 0 ? 
                totalNetDue.divide(BigDecimal.valueOf(usersWithDues), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDueAmount", totalNetDue);
            summary.put("totalUsers", usersWithDues);
            summary.put("averageDue", averageDue);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test endpoint to check users and their plot sizes
     */
    @GetMapping("/debug/users")
    public ResponseEntity<List<Map<String, Object>>> debugUsers() {
        List<User> allUsers = userService.findAll();
        
        List<Map<String, Object>> userInfo = allUsers.stream()
            .map(user -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                info.put("name", user.getOwnerName());
                info.put("plotNo", user.getPlotNo());
                info.put("plotSize", user.getPlotSizeMarla());
                info.put("hasPlotSize", user.getPlotSizeMarla() != null && !user.getPlotSizeMarla().isEmpty());
                return info;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Check if monthly payment exists for specific month/year
     */
    @GetMapping("/check-existing/{month}/{year}")
    public ResponseEntity<Map<String, Object>> checkExistingPayment(@PathVariable Integer month, @PathVariable Integer year) {
        Optional<MonthlyPayment> existing = monthlyPaymentService.getMonthlyPaymentByMonthAndYear(month, year);
        
        Map<String, Object> result = new HashMap<>();
        result.put("exists", existing.isPresent());
        
        if (existing.isPresent()) {
            MonthlyPayment payment = existing.get();
            result.put("payment", payment);
            result.put("message", "Monthly payment already exists for " + getMonthName(month) + " " + year);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a monthly payment configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMonthlyPayment(@PathVariable Long id, Authentication authentication) {
        try {
            // Get current user
            String userEmail = authentication.getName();
            User currentUser = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has Finance Secretary role (handle both formats)
            String userRole = currentUser.getRole();
            boolean isFinanceSecretary = "Finance Secretary".equals(userRole) || 
                                       "Finance-Secretary".equals(userRole) ||
                                       "FINANCE_SECRETARY".equals(userRole);
            
            if (!isFinanceSecretary) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only Finance Secretary can delete monthly payments. Current role: " + userRole));
            }

            monthlyPaymentService.deleteMonthlyPayment(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Monthly payment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method
    private String getMonthName(Integer month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "Unknown";
    }

    // DTOs for response
    public static class UserAccountInfo {
        private Long userId;
        private String userName;
        private String plotNo;
        private String plotSize;
        private String propertyType;
        private String builtStatus;
        private String email;
        private String phoneNo;
        private BigDecimal debitBalance;
        private BigDecimal totalDue;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getPlotNo() { return plotNo; }
        public void setPlotNo(String plotNo) { this.plotNo = plotNo; }
        public String getPlotSize() { return plotSize; }
        public void setPlotSize(String plotSize) { this.plotSize = plotSize; }
        public String getPropertyType() { return propertyType; }
        public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
        public String getBuiltStatus() { return builtStatus; }
        public void setBuiltStatus(String builtStatus) { this.builtStatus = builtStatus; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNo() { return phoneNo; }
        public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
        public BigDecimal getDebitBalance() { return debitBalance; }
        public void setDebitBalance(BigDecimal debitBalance) { this.debitBalance = debitBalance; }
        public BigDecimal getTotalDue() { return totalDue; }
        public void setTotalDue(BigDecimal totalDue) { this.totalDue = totalDue; }
    }

    public static class UserAccountDetails {
        private User user;
        private List<MonthlyPayment> allSetMonthlyPayments;
        private List<Due> paymentHistory;
        private BigDecimal debitAmount;
        private List<CustomDue> customDues;
        private BigDecimal totalCustomDueAmount;

        // Getters and setters
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
        public List<MonthlyPayment> getAllSetMonthlyPayments() { return allSetMonthlyPayments; }
        public void setAllSetMonthlyPayments(List<MonthlyPayment> allSetMonthlyPayments) { this.allSetMonthlyPayments = allSetMonthlyPayments; }
        public List<Due> getPaymentHistory() { return paymentHistory; }
        public void setPaymentHistory(List<Due> paymentHistory) { this.paymentHistory = paymentHistory; }
        public BigDecimal getDebitAmount() { return debitAmount; }
        public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
        public List<CustomDue> getCustomDues() { return customDues; }
        public void setCustomDues(List<CustomDue> customDues) { this.customDues = customDues; }
        public BigDecimal getTotalCustomDueAmount() { return totalCustomDueAmount; }
        public void setTotalCustomDueAmount(BigDecimal totalCustomDueAmount) { this.totalCustomDueAmount = totalCustomDueAmount; }
    }
    
    @GetMapping("/analysis/{month}/{year}")
    public ResponseEntity<?> getMonthlyPaymentAnalysis(
            @PathVariable int month, 
            @PathVariable int year) {
        try {
            List<MonthlyPayment> monthlyPayments = monthlyPaymentService.findByMonthAndYear(month, year);
            
            Map<String, Object> analysis = new HashMap<>();
            Map<String, Object> statusAnalysis = new HashMap<>();
            BigDecimal totalExpected = BigDecimal.ZERO;
            BigDecimal totalPlotSize = BigDecimal.ZERO;
            
            // Initialize status analysis
            String[] statuses = {"Completed", "Under-Construction", "Vacant", "Residential"};
            for (String status : statuses) {
                Map<String, Object> statusData = new HashMap<>();
                statusData.put("expectedAmount", BigDecimal.ZERO);
                statusData.put("ratePerMarla", BigDecimal.ZERO);
                statusData.put("totalPlotSize", BigDecimal.ZERO);
                statusData.put("plotCount", 0);
                statusAnalysis.put(status, statusData);
            }
            
            // Group by built status and calculate totals
            Map<String, List<MonthlyPayment>> paymentsByStatus = monthlyPayments.stream()
                .collect(java.util.stream.Collectors.groupingBy(MonthlyPayment::getBuiltStatus));
            
            for (Map.Entry<String, List<MonthlyPayment>> entry : paymentsByStatus.entrySet()) {
                String builtStatus = entry.getKey();
                List<MonthlyPayment> payments = entry.getValue();
                
                if (builtStatus != null && statusAnalysis.containsKey(builtStatus)) {
                    Map<String, Object> statusData = (Map<String, Object>) statusAnalysis.get(builtStatus);
                    
                    BigDecimal statusTotalAmount = BigDecimal.ZERO;
                    BigDecimal statusTotalPlotSize = BigDecimal.ZERO;
                    BigDecimal statusRate = BigDecimal.ZERO;
                    int plotCount = payments.size();
                    
                    for (MonthlyPayment payment : payments) {
                        BigDecimal rate = payment.getRatePerMarla() != null ? payment.getRatePerMarla() : BigDecimal.ZERO;
                        
                        // Get total plot size for this built status from all users
                        List<User> usersOfThisStatus = userService.findByBuiltStatus(builtStatus);
                        BigDecimal statusPlotSize = BigDecimal.ZERO;
                        
                        for (User user : usersOfThisStatus) {
                            if (user.getPlotSizeMarla() != null && !user.getPlotSizeMarla().isEmpty()) {
                                try {
                                    BigDecimal userPlotSize = new BigDecimal(user.getPlotSizeMarla());
                                    statusPlotSize = statusPlotSize.add(userPlotSize);
                                } catch (NumberFormatException e) {
                                    // Skip invalid plot sizes
                                }
                            }
                        }
                        
                        BigDecimal expectedAmount = rate.multiply(statusPlotSize);
                        statusTotalAmount = statusTotalAmount.add(expectedAmount);
                        statusTotalPlotSize = statusTotalPlotSize.add(statusPlotSize);
                        statusRate = rate; // Assuming same rate for all plots of same status
                        plotCount = usersOfThisStatus.size();
                        break; // Only need to calculate once per status
                    }
                    
                    statusData.put("expectedAmount", statusTotalAmount);
                    statusData.put("ratePerMarla", statusRate);
                    statusData.put("totalPlotSize", statusTotalPlotSize);
                    statusData.put("plotCount", plotCount);
                    
                    totalExpected = totalExpected.add(statusTotalAmount);
                    totalPlotSize = totalPlotSize.add(statusTotalPlotSize);
                }
            }
            
            analysis.put("statusAnalysis", statusAnalysis);
            analysis.put("totalExpected", totalExpected);
            analysis.put("totalPlotSize", totalPlotSize);
            analysis.put("month", month);
            analysis.put("year", year);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get monthly payment analysis: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}