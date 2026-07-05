package com.jeewanavenue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jeewanavenue.entity.Financial;
import com.jeewanavenue.repository.FinancialRepository;

@Service
public class FinancialService {

    @Autowired
    private FinancialRepository financialRepository;

    public List<Financial> findAll() {
        return financialRepository.findAll();
    }

    public Optional<Financial> findById(Long id) {
        return financialRepository.findById(id);
    }

    public Financial save(Financial financial) {
        return financialRepository.save(financial);
    }

    public Financial update(Long id, Financial financialDetails) {
        Financial financial = financialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financial record not found for id :: " + id));

        financial.setType(financialDetails.getType());
        financial.setDate(financialDetails.getDate());
        financial.setDescription(financialDetails.getDescription());
        financial.setCategory(financialDetails.getCategory());
        financial.setAmount(financialDetails.getAmount());
        financial.setPaymentMethod(financialDetails.getPaymentMethod());
        financial.setPlotNo(financialDetails.getPlotNo());
        financial.setPhoneNo(financialDetails.getPhoneNo());

        if (financialDetails.getReceiptPath() != null) {
            financial.setReceiptPath(financialDetails.getReceiptPath());
        }

        return financialRepository.save(financial);
    }

    public List<Financial> findByPlotNo(String plotNo) {
        return financialRepository.findByPlotNo(plotNo);
    }
    
    public List<Financial> findIncomeByMonthAndYear(int month, int year) {
        return financialRepository.findIncomeByMonthAndYear(month, year);
    }

    public void deleteById(Long id) {
        financialRepository.deleteById(id);
    }
}