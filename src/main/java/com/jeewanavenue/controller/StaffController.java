package com.jeewanavenue.controller;

import com.jeewanavenue.entity.Staff;
import com.jeewanavenue.service.FileStorageService;
import com.jeewanavenue.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<Staff> getAllStaff() {
        return staffService.findAll();
    }

    @PostMapping
    public ResponseEntity<Staff> createStaff(
            @RequestPart("staff") Staff staff,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String filePath = fileStorageService.storeFile(profilePicture, "profiles");
            staff.setProfilePicturePath(filePath);
        }

        Staff createdStaff = staffService.save(staff);
        return new ResponseEntity<>(createdStaff, HttpStatus.CREATED);
    }

    // --- THIS IS THE MISSING METHOD THAT IS NOW ADDED ---
    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(
            @PathVariable Long id,
            @RequestPart("staff") Staff staffDetails,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        try {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String filePath = fileStorageService.storeFile(profilePicture, "profiles");
                staffDetails.setProfilePicturePath(filePath);
            }
            Staff updatedStaff = staffService.update(id, staffDetails);
            return ResponseEntity.ok(updatedStaff);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}