package com.jeewanavenue.service;

import com.jeewanavenue.entity.UserCredit;
import com.jeewanavenue.repository.UserCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserCreditService {

    @Autowired
    private UserCreditRepository userCreditRepository;

    public List<UserCredit> getAllUserCredits() {
        return userCreditRepository.findAll();
    }

    public List<UserCredit> getAllUsersWithPositiveCredit() {
        return userCreditRepository.findAllWithPositiveCredit();
    }

    public Optional<UserCredit> getUserCreditById(Long id) {
        return userCreditRepository.findById(id);
    }

    public List<UserCredit> getCreditsByUserId(Long userId) {
        return userCreditRepository.findByUserId(userId);
    }

    public List<UserCredit> getCreditsByPlotNo(String plotNo) {
        return userCreditRepository.findByPlotNo(plotNo);
    }

    public BigDecimal getTotalCreditByUserId(Long userId) {
        return userCreditRepository.getTotalCreditByUserId(userId);
    }

    public BigDecimal getTotalCreditByPlotNo(String plotNo) {
        return userCreditRepository.getTotalCreditByPlotNo(plotNo);
    }

    @Transactional
    public UserCredit addCredit(Long userId, String userName, String plotNo, BigDecimal amount, String description) {
        UserCredit credit = new UserCredit(userId, userName, plotNo, amount, description);
        return userCreditRepository.save(credit);
    }

    @Transactional
    public BigDecimal useCredit(String plotNo, BigDecimal amountToUse, String description) {
        List<UserCredit> credits = getCreditsByPlotNo(plotNo);
        BigDecimal totalUsed = BigDecimal.ZERO;
        BigDecimal remainingToUse = amountToUse;
        
        for (UserCredit credit : credits) {
            if (remainingToUse.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            if (credit.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal creditAvailable = credit.getCreditAmount();
                BigDecimal amountUsedFromThisCredit;
                
                if (creditAvailable.compareTo(remainingToUse) >= 0) {
                    // This credit has enough to cover remaining amount
                    amountUsedFromThisCredit = remainingToUse;
                    credit.setCreditAmount(creditAvailable.subtract(remainingToUse));
                    remainingToUse = BigDecimal.ZERO;
                } else {
                    // Use all of this credit
                    amountUsedFromThisCredit = creditAvailable;
                    credit.setCreditAmount(BigDecimal.ZERO);
                    remainingToUse = remainingToUse.subtract(creditAvailable);
                }
                
                // Create negative entry to record usage
                addCredit(credit.getUserId(), credit.getUserName(), plotNo, 
                         amountUsedFromThisCredit.negate(), description);
                
                totalUsed = totalUsed.add(amountUsedFromThisCredit);
                userCreditRepository.save(credit);
            }
        }
        
        return totalUsed;
    }

    public UserCredit saveUserCredit(UserCredit userCredit) {
        return userCreditRepository.save(userCredit);
    }

    public void deleteUserCredit(Long id) {
        userCreditRepository.deleteById(id);
    }

    public List<UserCredit> getUserCreditsWithAmountGreaterThan(BigDecimal amount) {
        return userCreditRepository.findByCreditAmountGreaterThan(amount);
    }
}