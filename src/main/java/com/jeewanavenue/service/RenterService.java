package com.jeewanavenue.service;

import com.jeewanavenue.entity.Renter;
import com.jeewanavenue.repository.RenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RenterService {
    @Autowired
    private RenterRepository renterRepository;

    public List<Renter> findAll() {
        return renterRepository.findAll();
    }

    public Renter save(Renter renter) {
        return renterRepository.save(renter);
    }

    public void deleteById(Long id) {
        renterRepository.deleteById(id);
    }
}