package com.jeewanavenue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jeewanavenue.entity.Due;
import com.jeewanavenue.repository.DueRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialSchedulerService {

    @Autowired
    private DueRepository dueRepository;

    /**
     * Runs daily at 9:00 AM to check for overdue payments and apply late charges
     * Late charges are applied only ONCE per due - not daily accumulation
     */
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9:00 AM
    public void applyLateCharges() {
        System.out.println("Starting daily late charges check at " + LocalDate.now());
        
        try {
            // Get only unpaid dues that haven't had late charges applied yet
            List<Due> unpaidDuesWithoutLateCharges = dueRepository.findByDueAmountGreaterThanAndLateChargesAppliedFalse(BigDecimal.ZERO);
            
            int processedDues = 0;
            int lateChargesApplied = 0;
            int skippedNotOverdue = 0;
            BigDecimal totalLateCharges = BigDecimal.ZERO;
            
            LocalDate today = LocalDate.now();
            
            for (Due due : unpaidDuesWithoutLateCharges) {
                try {
                    // Calculate due date (issue date + gap days)
                    LocalDate dueDate = due.getIssueDate().plusDays(due.getGapDays());
                    
                    // Check if payment is overdue
                    if (today.isAfter(dueDate)) {
                        // Calculate how many days overdue
                        long daysOverdue = dueDate.until(today).getDays();
                        
                        // Apply late charges only if overdue (no grace period check needed since we're checking if today > dueDate)
                        if (daysOverdue > 0) {
                            // Get current late charges rate for this due
                            BigDecimal lateChargeRate = due.getLateCharges();
                            if (lateChargeRate == null) {
                                lateChargeRate = BigDecimal.valueOf(100); // Default PKR 100 total
                            }
                            
                            // Apply late charges ONCE (not per day) - just the fixed amount
                            BigDecimal lateChargesAmount = lateChargeRate; // Fixed amount, not multiplied by days
                            
                            // Add late charges to due amount
                            BigDecimal newDueAmount = due.getDueAmount().add(lateChargesAmount);
                            due.setDueAmount(newDueAmount);
                            
                            // Mark that late charges have been applied to prevent future applications
                            due.setLateChargesApplied(true);
                            
                            // Update the record
                            dueRepository.save(due);
                            
                            lateChargesApplied++;
                            totalLateCharges = totalLateCharges.add(lateChargesAmount);
                            
                            System.out.println("Applied late charges to " + due.getUserName() + 
                                " (Plot: " + due.getPlotNo() + ") - " + daysOverdue + " days overdue, " +
                                "One-time charges: PKR " + lateChargesAmount + ", New Total: PKR " + newDueAmount);
                        }
                    } else {
                        skippedNotOverdue++;
                    }
                    
                    processedDues++;
                    
                } catch (Exception e) {
                    System.err.println("Error processing due for user " + due.getUserName() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Late charges check completed:");
            System.out.println("- Processed: " + processedDues + " dues");
            System.out.println("- Late charges applied: " + lateChargesApplied + " users");
            System.out.println("- Skipped (not overdue): " + skippedNotOverdue + " users");
            System.out.println("- Total late charges added: PKR " + totalLateCharges);
            System.out.println("- Late charges are applied ONCE per due, not daily accumulation");
            
        } catch (Exception e) {
            System.err.println("Error in daily late charges check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manual method to apply late charges - can be called via API if needed
     */
    public String manualLateChargesCheck() {
        System.out.println("Manual late charges check initiated");
        applyLateCharges();
        return "Late charges check completed successfully";
    }
}