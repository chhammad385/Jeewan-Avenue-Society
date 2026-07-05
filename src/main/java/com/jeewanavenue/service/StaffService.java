package com.jeewanavenue.service;

import com.jeewanavenue.entity.Staff;
import com.jeewanavenue.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    public Staff update(Long id, Staff staffDetails) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));

        staff.setName(staffDetails.getName());
        staff.setPhoneNo(staffDetails.getPhoneNo());
        staff.setAddress(staffDetails.getAddress());
        staff.setPosition(staffDetails.getPosition());
        staff.setCategory(staffDetails.getCategory());
        staff.setJoiningDate(staffDetails.getJoiningDate());
        staff.setLeavingDate(staffDetails.getLeavingDate());
        staff.setStatus(staffDetails.getStatus());

        if (staffDetails.getProfilePicturePath() != null) {
            staff.setProfilePicturePath(staffDetails.getProfilePicturePath());
        }

        return staffRepository.save(staff);
    }

    public void deleteById(Long id) {
        staffRepository.deleteById(id);
    }
}