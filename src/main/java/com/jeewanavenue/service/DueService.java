package com.jeewanavenue.service;

import com.jeewanavenue.entity.Due;
import com.jeewanavenue.repository.DueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DueService {

    @Autowired
    private DueRepository dueRepository;

    @Autowired
    private CustomDueService customDueService;

    public List<Due> getAllDues() {
        return dueRepository.findAll();
    }

    public Optional<Due> getDueById(Long id) {
        return dueRepository.findById(id);
    }

    public List<Due> getDuesByUserId(Long userId) {
        return dueRepository.findByUserId(userId);
    }

    public List<Due> getDuesByMonthAndYear(Integer month, Integer year) {
        return dueRepository.findByMonthAndYear(month, year);
    }

    public List<Due> getDuesByUserName(String userName) {
        return dueRepository.findByUserNameContainingIgnoreCase(userName);
    }

    public BigDecimal getTotalDueAmountByUserId(Long userId) {
        // Get regular monthly dues
        List<Due> userDues = getDuesByUserId(userId);
        BigDecimal monthlyDues = userDues.stream()
                .map(Due::getDueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get unpaid custom dues
        BigDecimal customDues = customDueService.getTotalUnpaidAmountByUserId(userId);
        
        // Return total of both regular and custom dues
        return monthlyDues.add(customDues);
    }

    public List<Due> getByMonthYearAndUserName(Integer month, Integer year, String userName) {
        return dueRepository.findByMonthAndYearAndUserNameContainingIgnoreCase(month, year, userName);
    }

    public List<Due> getByMonthAndYear(Integer month, Integer year) {
        return dueRepository.findByMonthAndYear(month, year);
    }

    public List<Due> getByUserName(String userName) {
        return dueRepository.findByUserNameContainingIgnoreCase(userName);
    }

    public List<Due> getAll() {
        return dueRepository.findAll();
    }

    public BigDecimal getNetDueAmountByUserId(Long userId) {
        return getTotalDueAmountByUserId(userId);
    }

    public Optional<Due> findById(Long id) {
        return dueRepository.findById(id);
    }

    public void deleteById(Long id) {
        dueRepository.deleteById(id);
    }

    public Due save(Due due) {
        return dueRepository.save(due);
    }

    public Due saveDue(Due due) {
        return dueRepository.save(due);
    }

    @Transactional
    public Due updateDue(Long id, Due dueDetails) {
        Due due = dueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Due record not found for id :: " + id));

        due.setDueAmount(dueDetails.getDueAmount());
        due.setLateCharges(dueDetails.getLateCharges());
        due.setLateChargesApplied(dueDetails.getLateChargesApplied());
        
        return dueRepository.save(due);
    }

    public void deleteDue(Long id) {
        dueRepository.deleteById(id);
    }

    public List<Due> getUnpaidDues() {
        return dueRepository.findByDueAmountGreaterThan(BigDecimal.ZERO);
    }

    public List<Due> getOverdueDues() {
        return dueRepository.findByDueAmountGreaterThanAndLateChargesAppliedFalse(BigDecimal.ZERO);
    }

    @Transactional
    public void applyLateCharges() {
        LocalDate today = LocalDate.now();
        List<Due> overdueDues = getOverdueDues();
        
        for (Due due : overdueDues) {
            LocalDate dueDate = due.getIssueDate().plusDays(due.getGapDays());
            
            if (today.isAfter(dueDate) && !due.getLateChargesApplied()) {
                due.setDueAmount(due.getDueAmount().add(due.getLateCharges()));
                due.setLateChargesApplied(true);
                dueRepository.save(due);
            }
        }
    }

    public Due findExistingDue(Long userId, Integer month, Integer year) {
        return dueRepository.findByUserIdAndMonthAndYear(userId, month, year);
    }
}