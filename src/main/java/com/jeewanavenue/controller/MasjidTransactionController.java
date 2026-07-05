package com.jeewanavenue.controller;

import com.jeewanavenue.entity.MasjidTransaction;
import com.jeewanavenue.service.MasjidTransactionService;
import com.jeewanavenue.service.MasjidSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/masjid")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class MasjidTransactionController {

    @Autowired
    private MasjidTransactionService service;
    
    @Autowired
    private MasjidSettingsService settingsService;
    
    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;

    /**
     * GET /api/masjid/income - Get all income transactions
     */
    @GetMapping("/income")
    public ResponseEntity<List<MasjidTransaction>> getAllIncome() {
        List<MasjidTransaction> incomeList = service.getAllIncome();
        return ResponseEntity.ok(incomeList);
    }

    /**
     * GET /api/masjid/expense - Get all expense transactions
     */
    @GetMapping("/expense")
    public ResponseEntity<List<MasjidTransaction>> getAllExpense() {
        List<MasjidTransaction> expenseList = service.getAllExpense();
        return ResponseEntity.ok(expenseList);
    }

    /**
     * GET /api/masjid/transactions - Get all transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<MasjidTransaction>> getAllTransactions() {
        List<MasjidTransaction> transactions = service.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/masjid/transactions/{id} - Get transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<MasjidTransaction> getTransactionById(@PathVariable Long id) {
        return service.getTransactionById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/masjid/transactions - Create new transaction
     */
    @PostMapping("/transactions")
    public ResponseEntity<MasjidTransaction> createTransaction(@RequestBody MasjidTransaction transaction) {
        try {
            MasjidTransaction savedTransaction = service.saveTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/masjid/upload-transaction - Create transaction with optional file upload
     */
    @PostMapping("/upload-transaction")
    public ResponseEntity<MasjidTransaction> uploadTransaction(
            @RequestParam("type") String type,
            @RequestParam("date") String date,
            @RequestParam("category") String category,
            @RequestParam("amount") String amount,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "receipt", required = false) MultipartFile receipt) {
        
        try {
            // Save receipt file if present
            String receiptPath = null;
            if (receipt != null && !receipt.isEmpty()) {
                receiptPath = saveReceiptFile(receipt);
            }
            
            // Create transaction entity
            MasjidTransaction transaction = new MasjidTransaction();
            transaction.setType(type);
            transaction.setDate(LocalDate.parse(date));
            transaction.setCategory(category);
            transaction.setAmount(new BigDecimal(amount));
            transaction.setVendor(vendor);
            transaction.setSource(source);
            transaction.setDescription(description);
            transaction.setReceiptPath(receiptPath);
            
            // Save to database
            MasjidTransaction savedTransaction = service.saveTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Helper method to save receipt file
     */
    private String saveReceiptFile(MultipartFile file) throws IOException {
        // Create masjid-receipts directory if it doesn't exist
        String receiptDir = uploadDir + "masjid-receipts/";
        File directory = new File(receiptDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filepath = Paths.get(receiptDir + filename);
        Files.write(filepath, file.getBytes());
        
        // Return relative path for database storage
        return "uploads/masjid-receipts/" + filename;
    }

    /**
     * PUT /api/masjid/transactions/{id} - Update transaction
     */
    @PutMapping("/transactions/{id}")
    public ResponseEntity<MasjidTransaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody MasjidTransaction transaction) {
        try {
            MasjidTransaction updatedTransaction = service.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/masjid/transactions/{id} - Delete transaction
     */
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            service.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/masjid/financial-summary - Get overall financial summary
     * Optional query params: startDate and endDate for filtering by date range
     */
    @GetMapping("/financial-summary")
    public ResponseEntity<Map<String, BigDecimal>> getFinancialSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, BigDecimal> summary;
        
        if (startDate != null && endDate != null) {
            summary = service.getFinancialSummaryByDateRange(startDate, endDate);
        } else {
            summary = service.getFinancialSummary();
        }
        
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/masjid/transactions/date-range - Get transactions by date range
     */
    @GetMapping("/transactions/date-range")
    public ResponseEntity<List<MasjidTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<MasjidTransaction> transactions = service.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/masjid/transactions/category/{category} - Get transactions by category
     */
    @GetMapping("/transactions/category/{category}")
    public ResponseEntity<List<MasjidTransaction>> getTransactionsByCategory(@PathVariable String category) {
        List<MasjidTransaction> transactions = service.getTransactionsByCategory(category);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * POST /api/masjid/validate-code - Validate secret code
     */
    @PostMapping("/validate-code")
    public ResponseEntity<Map<String, Object>> validateSecretCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = settingsService.validateSecretCode(code);
        response.put("valid", isValid);
        response.put("message", isValid ? "Code validated successfully" : "Invalid secret code");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/masjid/set-secret-code - Set or update secret code (President only)
     */
    @PostMapping("/set-secret-code")
    public ResponseEntity<Map<String, Object>> setSecretCode(@RequestBody Map<String, String> payload) {
        String newCode = payload.get("code");
        String updatedBy = payload.get("updatedBy");
        
        Map<String, Object> response = new HashMap<>();
        
        if (newCode == null || newCode.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Secret code cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (newCode.trim().length() < 4) {
            response.put("success", false);
            response.put("message", "Secret code must be at least 4 characters");
            return ResponseEntity.badRequest().body(response);
        }
        
        settingsService.setSecretCode(newCode.trim(), updatedBy);
        response.put("success", true);
        response.put("message", "Secret code set successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/masjid/check-code-status - Check if secret code is set
     */
    @GetMapping("/check-code-status")
    public ResponseEntity<Map<String, Boolean>> checkCodeStatus() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSet", settingsService.isSecretCodeSet());
        return ResponseEntity.ok(response);
    }
}
