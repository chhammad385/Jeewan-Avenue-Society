package com.jeewanavenue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // The PasswordEncoder is no longer needed
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        // Handle CNIC - convert empty string to null to avoid unique constraint violation
        String cnic = user.getCnic();
        if (cnic != null && cnic.trim().isEmpty()) {
            user.setCnic(null);
        }
        
        // The password is now saved as plain text
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // DEBUG: Log before update
        System.out.println("🔧 UserService.updateUser - BEFORE:");
        System.out.println("   Current plotSizeMarla in DB: " + user.getPlotSizeMarla());
        System.out.println("   New plotSizeMarla from request: " + userDetails.getPlotSizeMarla());
        System.out.println("   Property Type: " + userDetails.getPropertyType());

        // Update fields
        user.setOwnerName(userDetails.getOwnerName());
        user.setFatherName(userDetails.getFatherName());
        user.setPlotNo(userDetails.getPlotNo());
        user.setPlotSizeMarla(userDetails.getPlotSizeMarla()); // Added missing field
        user.setStatus(userDetails.getStatus());
        user.setPreviousAddress(userDetails.getPreviousAddress());
        
        // Handle CNIC - convert empty string to null to avoid unique constraint violation
        String cnic = userDetails.getCnic();
        if (cnic != null && cnic.trim().isEmpty()) {
            cnic = null;
        }
        user.setCnic(cnic);
        
        user.setFamilyMembers(userDetails.getFamilyMembers());
        user.setPhoneNo(userDetails.getPhoneNo());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setAccountStatus(userDetails.getAccountStatus()); // Added missing field
        user.setBuiltStatus(userDetails.getBuiltStatus()); // Added new field
        user.setBloodGroup(userDetails.getBloodGroup()); // Added new field
        
        // Update property type and related fields
        user.setPropertyType(userDetails.getPropertyType());
        user.setNoOfShops(userDetails.getNoOfShops());
        
        // Update renter details
        user.setRenterName(userDetails.getRenterName());
        user.setRenterPhoneNo(userDetails.getRenterPhoneNo());
        user.setRenterCnic(userDetails.getRenterCnic());
        user.setRenterPreviousAddress(userDetails.getRenterPreviousAddress());

        // Only update password if a new one is provided and it's not empty
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            // The password is now saved as plain text
            user.setPassword(userDetails.getPassword());
        }

        if (userDetails.getProfilePicturePath() != null) {
            user.setProfilePicturePath(userDetails.getProfilePicturePath());
        }

        // DEBUG: Log before save
        System.out.println("💾 UserService.updateUser - BEFORE SAVE:");
        System.out.println("   plotSizeMarla value to save: " + user.getPlotSizeMarla());

        User savedUser = userRepository.save(user);
        
        // DEBUG: Log after save
        System.out.println("✅ UserService.updateUser - AFTER SAVE:");
        System.out.println("   plotSizeMarla in saved user: " + savedUser.getPlotSizeMarla());
        
        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Additional methods required by controllers
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByPlotNo(String plotNo) {
        return userRepository.findByPlotNo(plotNo).orElse(null);
    }

    public List<User> findByBuiltStatus(String builtStatus) {
        return userRepository.findByBuiltStatus(builtStatus);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}