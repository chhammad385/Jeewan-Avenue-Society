package com.jeewanavenue.service;

import com.jeewanavenue.entity.MasjidTransaction;
import com.jeewanavenue.repository.MasjidTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MasjidTransactionService {

    @Autowired
    private MasjidTransactionRepository repository;

    /**
     * Get all income transactions
     */
    public List<MasjidTransaction> getAllIncome() {
        return repository.findByTypeOrderByDateDesc("Income");
    }

    /**
     * Get all expense transactions
     */
    public List<MasjidTransaction> getAllExpense() {
        return repository.findByTypeOrderByDateDesc("Expense");
    }

    /**
     * Get transaction by ID
     */
    public Optional<MasjidTransaction> getTransactionById(Long id) {
        return repository.findById(id);
    }

    /**
     * Save a new transaction
     */
    @Transactional
    public MasjidTransaction saveTransaction(MasjidTransaction transaction) {
        return repository.save(transaction);
    }

    /**
     * Update an existing transaction
     */
    @Transactional
    public MasjidTransaction updateTransaction(Long id, MasjidTransaction updatedTransaction) {
        return repository.findById(id)
            .map(transaction -> {
                transaction.setType(updatedTransaction.getType());
                transaction.setDate(updatedTransaction.getDate());
                transaction.setMonth(updatedTransaction.getMonth());
                transaction.setDescription(updatedTransaction.getDescription());
                transaction.setCategory(updatedTransaction.getCategory());
                transaction.setAmount(updatedTransaction.getAmount());
                transaction.setSource(updatedTransaction.getSource());
                transaction.setVendor(updatedTransaction.getVendor());
                transaction.setReceiptPath(updatedTransaction.getReceiptPath());
                return repository.save(transaction);
            })
            .orElseThrow(() -> new RuntimeException("Masjid transaction not found with id: " + id));
    }

    /**
     * Delete a transaction
     */
    @Transactional
    public void deleteTransaction(Long id) {
        repository.deleteById(id);
    }

    /**
     * Get financial summary (all time)
     */
    public Map<String, BigDecimal> getFinancialSummary() {
        BigDecimal totalIncome = repository.calculateTotalIncome();
        BigDecimal totalExpense = repository.calculateTotalExpense();
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", netBalance);
        
        return summary;
    }

    /**
     * Get financial summary for a date range
     */
    public Map<String, BigDecimal> getFinancialSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = repository.calculateTotalIncomeByDateRange(startDate, endDate);
        BigDecimal totalExpense = repository.calculateTotalExpenseByDateRange(startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", netBalance);
        
        return summary;
    }

    /**
     * Get transactions by date range
     */
    public List<MasjidTransaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }

    /**
     * Get transactions by type and date range
     */
    public List<MasjidTransaction> getTransactionsByTypeAndDateRange(String type, LocalDate startDate, LocalDate endDate) {
        return repository.findByTypeAndDateBetween(type, startDate, endDate);
    }

    /**
     * Get transactions by category
     */
    public List<MasjidTransaction> getTransactionsByCategory(String category) {
        return repository.findByCategoryOrderByDateDesc(category);
    }

    /**
     * Get all transactions
     */
    public List<MasjidTransaction> getAllTransactions() {
        return repository.findAllByOrderByDateDesc();
    }
}
