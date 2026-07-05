package com.jeewanavenue.service;

import com.jeewanavenue.entity.UserDebit;
import com.jeewanavenue.repository.UserDebitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserDebitService {

    @Autowired
    private UserDebitRepository userDebitRepository;

    public List<UserDebit> getAllUserDebits() {
        return userDebitRepository.findAll();
    }

    public List<UserDebit> getAllUsersWithPositiveDebit() {
        return userDebitRepository.findAllWithPositiveDebit();
    }

    public Optional<UserDebit> getUserDebitById(Long id) {
        return userDebitRepository.findById(id);
    }

    public Optional<UserDebit> getUserDebitByUserId(Long userId) {
        return userDebitRepository.findByUserId(userId);
    }

    public Optional<UserDebit> getUserDebitByPlotNo(String plotNo) {
        return userDebitRepository.findByPlotNo(plotNo);
    }

    public BigDecimal getTotalDebitByUserId(Long userId) {
        BigDecimal debit = userDebitRepository.getTotalDebitByUserId(userId);
        return debit != null ? debit : BigDecimal.ZERO;
    }

    public BigDecimal getTotalDebitByPlotNo(String plotNo) {
        BigDecimal debit = userDebitRepository.getTotalDebitByPlotNo(plotNo);
        return debit != null ? debit : BigDecimal.ZERO;
    }

    @Transactional
    public UserDebit addDebit(Long userId, String userName, String plotNo, BigDecimal amount, String description) {
        Optional<UserDebit> existingDebit = getUserDebitByUserId(userId);
        
        if (existingDebit.isPresent()) {
            UserDebit userDebit = existingDebit.get();
            userDebit.setDebitAmount(userDebit.getDebitAmount().add(amount));
            userDebit.setDescription(description);
            return userDebitRepository.save(userDebit);
        } else {
            UserDebit newDebit = new UserDebit(userId, userName, plotNo, amount, description);
            return userDebitRepository.save(newDebit);
        }
    }

    @Transactional
    public BigDecimal useDebit(String plotNo, BigDecimal amountToUse, String description) {
        Optional<UserDebit> userDebitOpt = getUserDebitByPlotNo(plotNo);
        
        if (userDebitOpt.isPresent()) {
            UserDebit userDebit = userDebitOpt.get();
            BigDecimal currentDebit = userDebit.getDebitAmount();
            
            if (currentDebit.compareTo(amountToUse) >= 0) {
                // Sufficient debit available
                userDebit.setDebitAmount(currentDebit.subtract(amountToUse));
                userDebit.setDescription(description);
                userDebitRepository.save(userDebit);
                return amountToUse;
            } else {
                // Use all available debit
                userDebit.setDebitAmount(BigDecimal.ZERO);
                userDebit.setDescription(description);
                userDebitRepository.save(userDebit);
                return currentDebit;
            }
        }
        
        return BigDecimal.ZERO;
    }

    @Transactional
    public UserDebit updateDebitAmount(String plotNo, BigDecimal newAmount, String reason) {
        Optional<UserDebit> userDebitOpt = getUserDebitByPlotNo(plotNo);
        
        if (userDebitOpt.isPresent()) {
            UserDebit userDebit = userDebitOpt.get();
            userDebit.setDebitAmount(newAmount);
            userDebit.setDescription(reason);
            return userDebitRepository.save(userDebit);
        } else {
            throw new RuntimeException("User debit record not found for plot: " + plotNo);
        }
    }

    public UserDebit saveUserDebit(UserDebit userDebit) {
        return userDebitRepository.save(userDebit);
    }

    public void deleteUserDebit(Long id) {
        userDebitRepository.deleteById(id);
    }

    public List<UserDebit> getUserDebitsWithAmountGreaterThan(BigDecimal amount) {
        return userDebitRepository.findByDebitAmountGreaterThan(amount);
    }
}