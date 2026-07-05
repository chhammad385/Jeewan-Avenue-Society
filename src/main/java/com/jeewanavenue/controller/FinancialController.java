package com.jeewanavenue.controller;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jeewanavenue.entity.Financial;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.service.FileStorageService;
import com.jeewanavenue.service.FinancialService;
import com.jeewanavenue.service.UserService;
import com.jeewanavenue.service.AccountTransactionService;

@RestController
@RequestMapping("/api/financials")
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    /**
     * Gets all financial records, with optional filtering by type, year, and month.
     * Used by Income-Accounts.html and Expense-accounts.html
     */
    @GetMapping
    public List<Financial> getAllFinancials(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<Financial> records = financialService.findAll();

        // Apply filters if they are provided
        if (type != null && !type.isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        if (year != null) {
            records = records.stream()
                    .filter(r -> r.getDate().getYear() == year)
                    .collect(Collectors.toList());
        }
        if (month != null) {
            records = records.stream()
                    .filter(r -> r.getDate().getMonthValue() == month)
                    .collect(Collectors.toList());
        }
        return records;
    }

    /**
     * Gets a single financial record by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Financial> getFinancialById(@PathVariable Long id) {
        return financialService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new financial record. Handles optional receipt upload.
     * Used by Financials-President Console.html
     * Updates user account balance automatically.
     */
    @PostMapping
    public ResponseEntity<Financial> createFinancial(
            @RequestPart("record") Financial financial,
            @RequestPart(value = "receiptPic", required = false) MultipartFile receiptPic
    ) {
        try {
            if (receiptPic != null && !receiptPic.isEmpty()) {
                String filePath = fileStorageService.storeFile(receiptPic, "receipts");
                financial.setReceiptPath(filePath);
            }
            
            // Save the financial record first
            Financial savedFinancial = financialService.save(financial);
            
            // Update user account balance ONLY if this is NOT a charity donation
            // and it's an income payment with plot number
            if ("Income".equalsIgnoreCase(financial.getType()) && 
                financial.getPlotNo() != null && !financial.getPlotNo().trim().isEmpty() &&
                (financial.getIsCharity() == null || !financial.getIsCharity())) {
                
                // Find user by plot number
                User user = userService.findByPlotNo(financial.getPlotNo());
                if (user != null) {
                    // For income: subtract payment from user's balance (reduces due amount)
                    // If balance becomes negative, it means user has debit (advance payment)
                    user.subtractFromBalance(financial.getAmount());
                    userService.save(user);
                    
                    System.out.println("Updated account balance for user " + user.getOwnerName() + 
                                     " (Plot: " + user.getPlotNo() + 
                                     ") - Payment: " + financial.getAmount() + 
                                     ", New Balance: " + user.getAccountBalance());
                    
                    // Log the transaction for detailed history
                    accountTransactionService.logIncomePayment(user.getId(), financial.getAmount(), 
                                                             financial.getDescription(), savedFinancial.getId());
                    System.out.println("Transaction logged for income payment: " + financial.getDescription());
                }
            } else if ("Income".equalsIgnoreCase(financial.getType()) && 
                      financial.getIsCharity() != null && financial.getIsCharity()) {
                    System.out.println("Charity donation recorded: " + financial.getDescription() + 
                                 " - Amount: " + financial.getAmount() + 
                                 " (No account balance update)");
            } else if ("Income".equalsIgnoreCase(financial.getType()) && 
                      "Contribution".equals(financial.getCategory())) {
                System.out.println("Contribution recorded: " + financial.getDescription() + 
                                 " - Amount: " + financial.getAmount() + 
                                 " (No account balance update)");
            }            return new ResponseEntity<>(savedFinancial, HttpStatus.CREATED);
            
        } catch (Exception e) {
            System.err.println("Error creating financial record: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Updates an existing financial record.
     * Also updates user account balance accordingly.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Financial> updateFinancial(@PathVariable Long id, @RequestBody Financial financialDetails) {
        try {
            // Get the existing financial record to reverse its balance effect
            Financial existingFinancial = financialService.findById(id).orElse(null);
            if (existingFinancial == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Reverse the effect of the old financial record (only if not charity)
            if ("Income".equalsIgnoreCase(existingFinancial.getType()) && 
                existingFinancial.getPlotNo() != null && !existingFinancial.getPlotNo().trim().isEmpty() &&
                (existingFinancial.getIsCharity() == null || !existingFinancial.getIsCharity())) {
                
                User user = userService.findByPlotNo(existingFinancial.getPlotNo());
                if (user != null) {
                    // Reverse the previous income effect (add back the amount)
                    user.addToBalance(existingFinancial.getAmount());
                    userService.save(user);
                }
            }
            
            // Update the financial record
            Financial updatedFinancial = financialService.update(id, financialDetails);
            
            // Apply the effect of the updated financial record (only if not charity)
            if ("Income".equalsIgnoreCase(financialDetails.getType()) && 
                financialDetails.getPlotNo() != null && !financialDetails.getPlotNo().trim().isEmpty() &&
                (financialDetails.getIsCharity() == null || !financialDetails.getIsCharity())) {
                
                User user = userService.findByPlotNo(financialDetails.getPlotNo());
                if (user != null) {
                    // Apply the new income effect (subtract the amount)
                    user.subtractFromBalance(financialDetails.getAmount());
                    userService.save(user);
                    
                    System.out.println("Updated account balance for user " + user.getOwnerName() + 
                                     " (Plot: " + user.getPlotNo() + 
                                     ") - Updated Payment: " + financialDetails.getAmount() + 
                                     ", New Balance: " + user.getAccountBalance());
                    
                    // Log the transaction for detailed history
                    accountTransactionService.logIncomePaymentEdit(user.getId(), 
                                                                 existingFinancial.getAmount(),
                                                                 financialDetails.getAmount(),
                                                                 financialDetails.getDescription(), 
                                                                 updatedFinancial.getId());
                    System.out.println("Transaction logged for income payment edit: " + financialDetails.getDescription());
                }
            }
            
            return ResponseEntity.ok(updatedFinancial);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a financial record.
     * Also reverses its effect on user account balance.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinancial(@PathVariable Long id) {
        try {
            // Get the existing financial record to reverse its balance effect
            Financial existingFinancial = financialService.findById(id).orElse(null);
            if (existingFinancial == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Reverse the effect of the financial record before deleting (only if not charity)
            if ("Income".equalsIgnoreCase(existingFinancial.getType()) && 
                existingFinancial.getPlotNo() != null && !existingFinancial.getPlotNo().trim().isEmpty() &&
                (existingFinancial.getIsCharity() == null || !existingFinancial.getIsCharity())) {
                
                User user = userService.findByPlotNo(existingFinancial.getPlotNo());
                if (user != null) {
                    // Reverse the income effect (add back the amount)
                    user.addToBalance(existingFinancial.getAmount());
                    userService.save(user);
                    
                    System.out.println("Reversed account balance for user " + user.getOwnerName() + 
                                     " (Plot: " + user.getPlotNo() + 
                                     ") - Deleted Payment: " + existingFinancial.getAmount() + 
                                     ", New Balance: " + user.getAccountBalance());
                    
                    // Log the transaction for detailed history
                    accountTransactionService.logIncomePaymentDeleted(user.getId(), 
                                                                    existingFinancial.getAmount(),
                                                                    existingFinancial.getDescription(), 
                                                                    existingFinancial.getId());
                    System.out.println("Transaction logged for income payment deletion: " + existingFinancial.getDescription());
                }
            }
            
            // Delete the financial record
            financialService.deleteById(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            System.err.println("Error deleting financial record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

// --- ADD THIS ENTIRE NEW METHOD ---
    /**
     * Gets financial records associated with the logged-in user's plot number.
     * Used by Your Records.html
     */
    @GetMapping("/my-records")
    public ResponseEntity<List<Financial>> getMyRecords(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }
        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail)
                .orElse(null);

        if (currentUser == null || currentUser.getPlotNo() == null || currentUser.getPlotNo().isEmpty()) {
            // Return an empty list if user or plot number isn't found
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Financial> records = financialService.findByPlotNo(currentUser.getPlotNo());
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/monthly-income/{month}/{year}")
    public ResponseEntity<?> getMonthlyIncomeAnalysis(@PathVariable int month, @PathVariable int year) {
        try {
            List<Financial> monthlyIncomes = financialService.findIncomeByMonthAndYear(month, year);
            
            java.math.BigDecimal totalIncome = monthlyIncomes.stream()
                .filter(income -> !Boolean.TRUE.equals(income.getIsCharity()) && 
                                 !"Charity".equals(income.getCategory()) &&
                                 !"Contribution".equals(income.getCategory())) // Exclude charity donations and contributions
                .map(Financial::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("totalIncome", totalIncome);
            response.put("incomeRecords", monthlyIncomes);
            response.put("month", month);
            response.put("year", year);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to get monthly income analysis: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}