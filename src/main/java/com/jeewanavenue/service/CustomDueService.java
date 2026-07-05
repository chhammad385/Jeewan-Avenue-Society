package com.jeewanavenue.service;

import com.jeewanavenue.entity.CustomDue;
import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.CustomDueRepository;
import com.jeewanavenue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomDueService {

    @Autowired
    private CustomDueRepository customDueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountTransactionService accountTransactionService;

    /**
     * Create a new custom due for a user
     */
    public CustomDue createCustomDue(Long userId, BigDecimal amount, String reason, 
                                   String category, String createdBy, LocalDateTime dueDate, String notes) {
        // Validate user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOpt.get();
        
        // Create custom due
        CustomDue customDue = new CustomDue();
        customDue.setUserId(userId);
        customDue.setUserName(user.getOwnerName());
        customDue.setPlotNo(user.getPlotNo());
        customDue.setAmount(amount);
        customDue.setReason(reason);
        customDue.setCategory(category);
        customDue.setCreatedBy(createdBy);
        customDue.setDueDate(dueDate);
        customDue.setNotes(notes);
        customDue.setIsPaid(false);

        // Save the custom due first
        CustomDue savedCustomDue = customDueRepository.save(customDue);
        
        // Add the custom due amount to user's account balance
        user.addToBalance(amount);
        userRepository.save(user);
        
        System.out.println("Added custom due to user account balance for " + user.getOwnerName() + 
                         " - Custom Due: " + amount + 
                         ", New Balance: " + user.getAccountBalance());
        
        // Log the transaction for detailed history
        accountTransactionService.logCustomDueAdded(userId, amount, reason, savedCustomDue.getId());
        System.out.println("Transaction logged for custom due added: " + reason);
        
        return savedCustomDue;
    }

    /**
     * Get all custom dues for a user
     */
    public List<CustomDue> getCustomDuesByUserId(Long userId) {
        return customDueRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all unpaid custom dues for a user
     */
    public List<CustomDue> getUnpaidCustomDuesByUserId(Long userId) {
        return customDueRepository.findByUserIdAndIsPaidFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get total unpaid custom due amount for a user
     */
    public BigDecimal getTotalUnpaidAmountByUserId(Long userId) {
        return customDueRepository.getTotalUnpaidAmountByUserId(userId);
    }

    /**
     * Get total custom due amount (paid and unpaid) for a user
     */
    public BigDecimal getTotalAmountByUserId(Long userId) {
        return customDueRepository.getTotalAmountByUserId(userId);
    }

    /**
     * Mark a custom due as paid
     */
    public CustomDue markAsPaid(Long customDueId, String paidBy) {
        Optional<CustomDue> customDueOpt = customDueRepository.findById(customDueId);
        if (!customDueOpt.isPresent()) {
            throw new RuntimeException("Custom due not found with ID: " + customDueId);
        }

        CustomDue customDue = customDueOpt.get();
        
        // Get user to update account balance
        Optional<User> userOpt = userRepository.findById(customDue.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Subtract the custom due amount from user's account balance (payment reduces balance)
            user.subtractFromBalance(customDue.getAmount());
            userRepository.save(user);
            
            System.out.println("Custom due payment updated account balance for " + user.getOwnerName() + 
                             " - Payment: " + customDue.getAmount() + 
                             ", New Balance: " + user.getAccountBalance());
            
            // Log the transaction for detailed history
            accountTransactionService.logCustomDuePaid(customDue.getUserId(), customDue.getAmount(), 
                                                     customDue.getReason(), customDue.getId());
            System.out.println("Transaction logged for custom due payment: " + customDue.getReason());
            
            // Log the transaction for detailed history
            accountTransactionService.logCustomDuePaid(user.getId(), customDue.getAmount(), 
                                                     customDue.getReason(), customDue.getId());
            System.out.println("Transaction logged for custom due payment: " + customDue.getReason());
        }
        
        customDue.setIsPaid(true);
        customDue.setPaidAt(LocalDateTime.now());
        customDue.setNotes((customDue.getNotes() != null ? customDue.getNotes() + " | " : "") + 
                          "Paid by: " + paidBy + " on " + LocalDateTime.now());

        return customDueRepository.save(customDue);
    }

    /**
     * Update a custom due
     */
    public CustomDue updateCustomDue(Long customDueId, BigDecimal amount, String reason, 
                                   String category, LocalDateTime dueDate, String notes) {
        Optional<CustomDue> customDueOpt = customDueRepository.findById(customDueId);
        if (!customDueOpt.isPresent()) {
            throw new RuntimeException("Custom due not found with ID: " + customDueId);
        }

        CustomDue customDue = customDueOpt.get();
        
        // Only allow updates if not paid
        if (customDue.getIsPaid()) {
            throw new RuntimeException("Cannot update a paid custom due");
        }

        customDue.setAmount(amount);
        customDue.setReason(reason);
        customDue.setCategory(category);
        customDue.setDueDate(dueDate);
        customDue.setNotes(notes);

        return customDueRepository.save(customDue);
    }

    /**
     * Delete a custom due (only if not paid)
     */
    public void deleteCustomDue(Long customDueId) {
        Optional<CustomDue> customDueOpt = customDueRepository.findById(customDueId);
        if (!customDueOpt.isPresent()) {
            throw new RuntimeException("Custom due not found with ID: " + customDueId);
        }

        CustomDue customDue = customDueOpt.get();
        
        // Only allow deletion if not paid
        if (customDue.getIsPaid()) {
            throw new RuntimeException("Cannot delete a paid custom due");
        }

        customDueRepository.delete(customDue);
    }

    /**
     * Get all custom dues
     */
    public List<CustomDue> getAllCustomDues() {
        return customDueRepository.findAll();
    }

    /**
     * Get all unpaid custom dues
     */
    public List<CustomDue> getAllUnpaidCustomDues() {
        return customDueRepository.findByIsPaidFalseOrderByCreatedAtDesc();
    }

    /**
     * Get custom due by ID
     */
    public Optional<CustomDue> getCustomDueById(Long id) {
        return customDueRepository.findById(id);
    }

    /**
     * Get custom dues by category
     */
    public List<CustomDue> getCustomDuesByCategory(String category) {
        return customDueRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    /**
     * Get custom dues created by specific admin
     */
    public List<CustomDue> getCustomDuesByCreatedBy(String createdBy) {
        return customDueRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    /**
     * Count unpaid custom dues for a user
     */
    public Long countUnpaidCustomDuesByUserId(Long userId) {
        return customDueRepository.countUnpaidByUserId(userId);
    }
}