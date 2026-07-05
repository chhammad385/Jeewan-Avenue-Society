package com.jeewanavenue.controller;

import com.jeewanavenue.entity.AccountTransaction;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.service.AccountTransactionService;
import com.jeewanavenue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/account-transactions")
public class AccountTransactionController {

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Autowired
    private UserService userService;

    /**
     * Get transaction history for the logged-in user
     * Used by Check Due page to show detailed transaction breakdown
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<Map<String, Object>> getMyTransactions(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        List<AccountTransaction> transactions = accountTransactionService.getUserTransactions(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalTransactions", transactions.size());
        response.put("currentBalance", currentUser.getAccountBalance());
        response.put("hasDue", currentUser.hasDue());
        response.put("hasDebit", currentUser.hasDebit());
        response.put("dueAmount", currentUser.hasDue() ? currentUser.getDueAmount() : 0);
        response.put("debitAmount", currentUser.hasDebit() ? currentUser.getDebitAmount() : 0);

        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction history for a specific user (admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRESIDENT')")
    public ResponseEntity<Map<String, Object>> getUserTransactions(@PathVariable Long userId) {
        User user = userService.findById(userId).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        List<AccountTransaction> transactions = accountTransactionService.getUserTransactions(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
            "id", user.getId(),
            "name", user.getOwnerName(),
            "plotNo", user.getPlotNo(),
            "accountBalance", user.getAccountBalance()
        ));
        response.put("transactions", transactions);
        response.put("totalTransactions", transactions.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get recent transactions for the logged-in user (last 10)
     */
    @GetMapping("/my-recent")
    public ResponseEntity<List<AccountTransaction>> getMyRecentTransactions(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404).build();
        }

        List<AccountTransaction> recentTransactions = accountTransactionService
            .getRecentUserTransactions(currentUser.getId(), 10);

        return ResponseEntity.ok(recentTransactions);
    }

    /**
     * Get transaction history within date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AccountTransaction>> getTransactionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404).build();
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            List<AccountTransaction> transactions = accountTransactionService
                .getUserTransactionsByDateRange(currentUser.getId(), start, end);

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get transaction statistics for a user
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyTransactionStats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = authentication.getName();
        User currentUser = userService.findByEmail(userEmail).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(404).build();
        }

        Long totalTransactions = accountTransactionService.getUserTransactionCount(currentUser.getId());
        List<AccountTransaction> recentTransactions = accountTransactionService
            .getRecentUserTransactions(currentUser.getId(), 5);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("currentBalance", currentUser.getAccountBalance());
        stats.put("accountStatus", currentUser.hasDue() ? "Due" : (currentUser.hasDebit() ? "Debit" : "Clear"));
        stats.put("recentTransactions", recentTransactions);

        return ResponseEntity.ok(stats);
    }
}