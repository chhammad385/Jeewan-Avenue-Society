package com.jeewanavenue.service;

import com.jeewanavenue.entity.MonthlyPayment;
import com.jeewanavenue.entity.Due;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.MonthlyPaymentRepository;
import com.jeewanavenue.repository.DueRepository;
import com.jeewanavenue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonthlyPaymentService {

    @Autowired
    private MonthlyPaymentRepository monthlyPaymentRepository;

    @Autowired
    private DueRepository dueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDebitService userDebitService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    public List<MonthlyPayment> getAllMonthlyPayments() {
        return monthlyPaymentRepository.findAllByOrderByYearDescMonthDesc();
    }

    public Optional<MonthlyPayment> getMonthlyPaymentById(Long id) {
        return monthlyPaymentRepository.findById(id);
    }

    public Optional<MonthlyPayment> getMonthlyPaymentByMonthAndYear(Integer month, Integer year) {
        return monthlyPaymentRepository.findByMonthAndYear(month, year);
    }

    public List<MonthlyPayment> getMonthlyPaymentsByYear(Integer year) {
        return monthlyPaymentRepository.findByYear(year);
    }
    
    public List<MonthlyPayment> findByMonthAndYear(Integer month, Integer year) {
        return monthlyPaymentRepository.findAllByMonthAndYear(month, year);
    }

    public MonthlyPayment saveMonthlyPayment(MonthlyPayment monthlyPayment) {
        return monthlyPaymentRepository.save(monthlyPayment);
    }

    @Transactional
    public MonthlyPayment setMonthlyPayment(MonthlyPayment monthlyPayment, List<Long> userIds, User financeSecretary) {
        System.out.println("=== SETTING MONTHLY PAYMENT ===");
        System.out.println("Month: " + monthlyPayment.getMonth() + " (" + getMonthName(monthlyPayment.getMonth()) + ")");
        System.out.println("Year: " + monthlyPayment.getYear());
        System.out.println("Property Type: " + monthlyPayment.getPropertyType());
        System.out.println("Built Status: " + monthlyPayment.getBuiltStatus());
        System.out.println("Rate per Marla: " + monthlyPayment.getRatePerMarla());
        System.out.println("Selected User IDs: " + (userIds != null ? userIds.size() : 0));
        
        // Check if monthly payment already exists for this month/year/builtStatus combination
        Optional<MonthlyPayment> existing = monthlyPaymentRepository.findByMonthAndYearAndBuiltStatus(
            monthlyPayment.getMonth(), monthlyPayment.getYear(), monthlyPayment.getBuiltStatus());
        
        if (existing.isPresent()) {
            System.out.println("EXISTING PAYMENT FOUND - ID: " + existing.get().getId());
            String builtStatusText = monthlyPayment.getBuiltStatus() != null ? 
                " for " + monthlyPayment.getBuiltStatus() + " status" : "";
            throw new RuntimeException("Monthly payment already exists for " + 
                getMonthName(monthlyPayment.getMonth()) + " " + monthlyPayment.getYear() + 
                builtStatusText + ". Please delete the existing payment first or update it instead.");
        }

        System.out.println("No existing payment found for this built status, creating new one...");

        // Save the monthly payment configuration
        MonthlyPayment savedPayment = saveMonthlyPayment(monthlyPayment);
        System.out.println("Monthly payment saved with ID: " + savedPayment.getId());

        // Generate dues for selected users
        int processedUsers = generateDuesForSelectedUsers(savedPayment, userIds, financeSecretary);
        
        System.out.println("Users processed for dues: " + processedUsers);
        
        if (processedUsers == 0) {
            String builtStatusText = monthlyPayment.getBuiltStatus() != null ? 
                " with " + monthlyPayment.getBuiltStatus() + " status" : "";
            throw new RuntimeException("Monthly payment created but no users" + builtStatusText + 
                " have plot sizes defined. Please ensure users have valid plot sizes before setting monthly payments.");
        }

        System.out.println("=== MONTHLY PAYMENT SETTING COMPLETED ===");
        return savedPayment;
    }

    @Transactional
    private int generateDuesForSelectedUsers(MonthlyPayment monthlyPayment, List<Long> userIds, User financeSecretary) {
        System.out.println("Generating dues for selected users...");
        System.out.println("User IDs: " + userIds);

        if (userIds == null || userIds.isEmpty()) {
            System.out.println("No users selected");
            return 0;
        }

        int processedUsers = 0;
        
        for (Long userId : userIds) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + userId);
                continue;
            }

            User user = userOpt.get();
            System.out.println("Processing User: " + user.getOwnerName() + ", Plot Size: " + user.getPlotSizeMarla());
            
            if (user.getPlotSizeMarla() != null && !user.getPlotSizeMarla().isEmpty() && !"N/A marla".equals(user.getPlotSizeMarla())) {
                try {
                    // Parse plot size - extract number from formats like "5 marla", "5.0 marla", "5"
                    String plotSizeStr = user.getPlotSizeMarla().trim();
                    
                    // Remove "marla" word if present (case insensitive)
                    plotSizeStr = plotSizeStr.replaceAll("(?i)\\\\s*marla\\\\s*$", "").trim();
                    
                    // Skip if the result is empty or "N/A"
                    if (plotSizeStr.isEmpty() || "N/A".equalsIgnoreCase(plotSizeStr)) {
                        System.out.println("Skipping user " + user.getOwnerName() + " - plot size is N/A or empty");
                        continue;
                    }

                    BigDecimal plotSize = new BigDecimal(plotSizeStr);
                    BigDecimal dueAmount = monthlyPayment.getRatePerMarla().multiply(plotSize);
                    
                    System.out.println("Calculating due: " + monthlyPayment.getRatePerMarla() + " * " + plotSize + " = " + dueAmount);

                    // Update user account balance
                    BigDecimal currentBalance = user.getAccountBalance();
                    BigDecimal newBalance = currentBalance.add(dueAmount);
                    user.setAccountBalance(newBalance);
                    userRepository.save(user);

                    // Log the transaction for detailed history
                    String monthYear = getMonthName(monthlyPayment.getMonth()) + " " + monthlyPayment.getYear();
                    accountTransactionService.logMonthlyPaymentDue(user.getId(), dueAmount, monthYear);
                    System.out.println("Transaction logged for monthly payment due: " + monthYear);

                    // Generate bill message for this user
                    String billMessage = generateBillMessage(user, monthlyPayment, dueAmount, financeSecretary);
                    System.out.println("\n========== BILL MESSAGE FOR " + user.getOwnerName() + " ==========");
                    System.out.println(billMessage);
                    System.out.println("==================================================\n");

                    // Store monthly payment information for record keeping (optional)
                    Due due = new Due();
                    due.setUserId(user.getId());
                    due.setUserName(user.getOwnerName());
                    due.setPlotNo(user.getPlotNo());
                    due.setAreaMarla(plotSize);
                    due.setDueAmount(dueAmount);
                    due.setMonth(monthlyPayment.getMonth());
                    due.setYear(monthlyPayment.getYear());
                    due.setDueMonth(getMonthName(monthlyPayment.getMonth()));
                    due.setDueYear(monthlyPayment.getYear());
                    due.setIssueDate(monthlyPayment.getIssueDate());
                    due.setGapDays(monthlyPayment.getGapDays());
                    due.setLateCharges(monthlyPayment.getLateCharges());
                    due.setLateChargesApplied(false);

                    dueRepository.save(due);
                    System.out.println("Due record saved for user: " + user.getOwnerName());
                    processedUsers++;

                } catch (NumberFormatException e) {
                    System.err.println("Invalid plot size format for user " + user.getOwnerName() + ": " + user.getPlotSizeMarla());
                }
            } else {
                System.out.println("Skipping user " + user.getOwnerName() + " - no valid plot size");
            }
        }
        
        System.out.println("Total users processed: " + processedUsers);
        return processedUsers;
    }

    @Transactional
    private int generateDuesForAllUsers(MonthlyPayment monthlyPayment) {
        List<User> allUsers = userRepository.findAll();
        
        System.out.println("Total users found: " + allUsers.size());
        System.out.println("Filtering by property type: " + monthlyPayment.getPropertyType());
        System.out.println("Filtering by built status: " + monthlyPayment.getBuiltStatus());
        
        // Filter users by property type and built status if specified
        List<User> filteredUsers = allUsers;
        
        // Filter by property type (Shop/House)
        if (monthlyPayment.getPropertyType() != null && !monthlyPayment.getPropertyType().isEmpty()) {
            filteredUsers = filteredUsers.stream()
                .filter(user -> monthlyPayment.getPropertyType().equals(user.getPropertyType()))
                .collect(Collectors.toList());
            System.out.println("Users with property type '" + monthlyPayment.getPropertyType() + "': " + filteredUsers.size());
        }
        
        // Filter by built status
        if (monthlyPayment.getBuiltStatus() != null && !monthlyPayment.getBuiltStatus().isEmpty()) {
            filteredUsers = filteredUsers.stream()
                .filter(user -> monthlyPayment.getBuiltStatus().equals(user.getBuiltStatus()))
                .collect(Collectors.toList());
            System.out.println("Users with built status '" + monthlyPayment.getBuiltStatus() + "': " + filteredUsers.size());
        }
        
        int usersWithPlotSize = 0;
        
        for (User user : filteredUsers) {
            System.out.println("User: " + user.getOwnerName() + ", Plot Size: " + user.getPlotSizeMarla() + ", Built Status: " + user.getBuiltStatus());
            
            if (user.getPlotSizeMarla() != null && !user.getPlotSizeMarla().isEmpty() && !"N/A marla".equals(user.getPlotSizeMarla())) {
                usersWithPlotSize++;
                try {
                    // Parse plot size - extract number from formats like "5 marla", "5.0 marla", "5"
                    String plotSizeStr = user.getPlotSizeMarla().trim();
                    
                    // Remove "marla" word if present (case insensitive)
                    plotSizeStr = plotSizeStr.replaceAll("(?i)\\s*marla\\s*$", "").trim();
                    
                    // Skip if the result is empty or "N/A"
                    if (plotSizeStr.isEmpty() || "N/A".equalsIgnoreCase(plotSizeStr)) {
                        System.out.println("Skipping user " + user.getOwnerName() + " - plot size is N/A or empty");
                        usersWithPlotSize--; // Decrement since this user doesn't have valid plot size
                        continue;
                    }
                    
                    BigDecimal plotSize = new BigDecimal(plotSizeStr);
                    
                    // Calculate due amount = rate per marla × plot size
                    BigDecimal dueAmount = monthlyPayment.getRatePerMarla().multiply(plotSize);
                    
                    System.out.println("Adding monthly payment to user account balance for " + user.getOwnerName() + 
                        ": Plot Size=" + plotSize + ", Due Amount=" + dueAmount);
                    
                    // Add the due amount to user's account balance (positive = due, negative = debit)
                    // If user already has debit (negative balance), this will either reduce the debit or create due
                    user.addToBalance(dueAmount);
                    userRepository.save(user);
                    
                    System.out.println("Updated account balance for user " + user.getOwnerName() + 
                                     " - Added Due: " + dueAmount + 
                                     ", New Balance: " + user.getAccountBalance());
                    
                    // Log the transaction for detailed history
                    String monthYear = getMonthName(monthlyPayment.getMonth()) + " " + monthlyPayment.getYear();
                    accountTransactionService.logMonthlyPaymentDue(user.getId(), dueAmount, monthYear);
                    System.out.println("Transaction logged for monthly payment due: " + monthYear);
                    
                    // Store monthly payment information for record keeping (optional)
                    // We can keep a simplified Due record just for reference without complex logic
                    Due due = new Due();
                    due.setUserId(user.getId());
                    due.setUserName(user.getOwnerName());
                    due.setPlotNo(user.getPlotNo());
                    due.setAreaMarla(plotSize);
                    due.setDueAmount(dueAmount); // Store original amount for reference
                    due.setMonth(monthlyPayment.getMonth());
                    due.setYear(monthlyPayment.getYear());
                    due.setDueMonth(getMonthName(monthlyPayment.getMonth()));
                    due.setDueYear(monthlyPayment.getYear());
                    due.setIssueDate(monthlyPayment.getIssueDate());
                    due.setGapDays(monthlyPayment.getGapDays());
                    due.setLateCharges(monthlyPayment.getLateCharges());
                    due.setLateChargesApplied(false);
                    
                    Due savedDue = dueRepository.save(due);
                    System.out.println("Due record saved for reference - user " + user.getOwnerName() + 
                        " with ID: " + savedDue.getId() + ", Amount: " + savedDue.getDueAmount());
                    
                } catch (NumberFormatException e) {
                    // Skip users with invalid plot size format
                    System.err.println("Invalid plot size format for user " + user.getOwnerName() + ": '" + user.getPlotSizeMarla() + "' - " + e.getMessage());
                    usersWithPlotSize--; // Decrement since this user doesn't have valid plot size
                } catch (Exception e) {
                    System.err.println("Error generating due for user " + user.getOwnerName() + ": " + e.getMessage());
                    usersWithPlotSize--; // Decrement since this user couldn't be processed
                }
            }
        }
        
        System.out.println("Due generation completed. Users with plot size: " + usersWithPlotSize + "/" + filteredUsers.size() + 
                          " (Built Status: " + monthlyPayment.getBuiltStatus() + ")");
        return usersWithPlotSize;
    }

    public void deleteMonthlyPayment(Long id) {
        monthlyPaymentRepository.deleteById(id);
    }

    private String getMonthName(Integer month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "Unknown";
    }

    /**
     * Generate monthly town fees bill message for a user based on property type
     * @param user The user for whom to generate the bill
     * @param monthlyPayment The monthly payment configuration
     * @param dueAmount The calculated due amount for the user
     * @param financeSecretary The current finance secretary setting the payment
     * @return Formatted WhatsApp/SMS message for the bill
     */
    public String generateBillMessage(User user, MonthlyPayment monthlyPayment, BigDecimal dueAmount, User financeSecretary) {
        String monthName = getMonthName(monthlyPayment.getMonth());
        String issueDate = monthlyPayment.getIssueDate().getDayOfMonth() + "-" + monthName + "-" + monthlyPayment.getYear();
        
        // Calculate due date (issue date + gap days)
        java.time.LocalDate dueDate = monthlyPayment.getIssueDate().plusDays(monthlyPayment.getGapDays());
        String dueDateStr = dueDate.getDayOfMonth() + "-" + getMonthName(dueDate.getMonthValue()) + "-" + dueDate.getYear();
        
        // Calculate bill amount after due date (add late charges)
        BigDecimal billAfterDueDate = dueAmount.add(monthlyPayment.getLateCharges());
        
        String propertyType = user.getPropertyType();
        String plotNo = user.getPlotNo() != null ? user.getPlotNo() : "N/A";
        String ownerName = user.getOwnerName() != null ? user.getOwnerName() : "Resident";
        
        // Get finance secretary info
        String fsName = financeSecretary != null && financeSecretary.getOwnerName() != null ? 
                       financeSecretary.getOwnerName() : "Muhammad Raza";
        String fsPlotNo = financeSecretary != null && financeSecretary.getPlotNo() != null ? 
                         financeSecretary.getPlotNo() : "H-248";
        String fsPhone = financeSecretary != null && financeSecretary.getPhoneNo() != null ? 
                        financeSecretary.getPhoneNo() : "03006913494";
        
        StringBuilder message = new StringBuilder();
        message.append("Monthly Town Fees Bill\n");
        message.append("Jeewan Avenue, Sahiwal\n\n");
        
        if ("Shop".equalsIgnoreCase(propertyType)) {
            // Shop-specific message
            message.append("Rate Per Shop: Rs. ").append(monthlyPayment.getRatePerMarla()).append("\n\n");
        } else {
            // House-specific message
            message.append("Rate Per Marla: Rs. ").append(monthlyPayment.getRatePerMarla()).append("\n\n");
        }
        
        message.append("Dear ").append(ownerName).append("\n");
        message.append("House/Shop No: ").append(plotNo).append("\n");
        message.append("Issue Date: ").append(issueDate).append("\n");
        message.append("Due Date: ").append(dueDateStr).append("\n");
        message.append("Bill Amount (Rs.): ").append(dueAmount.intValue()).append("\n");
        message.append("Bill After Due Date: ").append(billAfterDueDate.intValue()).append("\n\n");
        message.append("Thanks\n\n");
        message.append("Finance Secretary:\n");
        message.append(fsName).append("\n");
        message.append(fsPlotNo).append("\n");
        message.append(fsPhone);
        
        return message.toString();
    }

    /**
     * Process single user payment and return message for WhatsApp
     * This method processes payment for one user at a time
     */
    @Transactional
    public Map<String, Object> processSingleUserPayment(Long userId, MonthlyPayment monthlyPayment, User financeSecretary) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            result.put("success", false);
            result.put("error", "User not found");
            return result;
        }

        User user = userOpt.get();
        
        if (user.getPlotSizeMarla() == null || user.getPlotSizeMarla().isEmpty() || "N/A marla".equals(user.getPlotSizeMarla())) {
            result.put("success", false);
            result.put("error", "User does not have a valid plot size");
            return result;
        }

        try {
            // Parse plot size
            String plotSizeStr = user.getPlotSizeMarla().trim();
            plotSizeStr = plotSizeStr.replaceAll("(?i)\\s*marla\\s*$", "").trim();
            
            if (plotSizeStr.isEmpty() || "N/A".equalsIgnoreCase(plotSizeStr)) {
                result.put("success", false);
                result.put("error", "Invalid plot size");
                return result;
            }

            BigDecimal plotSize = new BigDecimal(plotSizeStr);
            BigDecimal dueAmount = monthlyPayment.getRatePerMarla().multiply(plotSize);
            
            // Update user account balance
            BigDecimal currentBalance = user.getAccountBalance();
            BigDecimal newBalance = currentBalance.add(dueAmount);
            user.setAccountBalance(newBalance);
            userRepository.save(user);

            // Log the transaction
            String monthYear = getMonthName(monthlyPayment.getMonth()) + " " + monthlyPayment.getYear();
            accountTransactionService.logMonthlyPaymentDue(user.getId(), dueAmount, monthYear);

            // Generate bill message
            String billMessage = generateBillMessage(user, monthlyPayment, dueAmount, financeSecretary);

            // Store due record
            Due due = new Due();
            due.setUserId(user.getId());
            due.setUserName(user.getOwnerName());
            due.setPlotNo(user.getPlotNo());
            due.setAreaMarla(plotSize);
            due.setDueAmount(dueAmount);
            due.setMonth(monthlyPayment.getMonth());
            due.setYear(monthlyPayment.getYear());
            due.setDueMonth(getMonthName(monthlyPayment.getMonth()));
            due.setDueYear(monthlyPayment.getYear());
            due.setIssueDate(monthlyPayment.getIssueDate());
            due.setGapDays(monthlyPayment.getGapDays());
            due.setLateCharges(monthlyPayment.getLateCharges());
            due.setLateChargesApplied(false);
            dueRepository.save(due);

            System.out.println("\n========== BILL MESSAGE FOR " + user.getOwnerName() + " ==========");
            System.out.println(billMessage);
            System.out.println("==================================================\n");

            result.put("success", true);
            result.put("message", billMessage);
            result.put("userName", user.getOwnerName());
            result.put("plotNo", user.getPlotNo());
            result.put("phoneNo", user.getPhoneNo());
            result.put("dueAmount", dueAmount);
            result.put("propertyType", user.getPropertyType());

        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("error", "Invalid plot size format: " + user.getPlotSizeMarla());
        }
        
        return result;
    }
}
