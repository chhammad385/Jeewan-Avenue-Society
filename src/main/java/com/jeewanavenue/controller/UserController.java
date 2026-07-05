package com.jeewanavenue.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.service.FileStorageService;
import com.jeewanavenue.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    // GET all users (For President Console)
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    // GET all users - explicit endpoint
    @GetMapping("/all")
    public List<User> getAllUsersExplicit() {
        return userService.findAllUsers();
    }

    // GET the currently logged-in user's profile
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = authentication.getName();
        return userService.findByEmail(userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST a new user (from President Console)
    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestPart("user") User user,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String filePath = fileStorageService.storeFile(profilePicture, "profiles");
            user.setProfilePicturePath(filePath);
        }

        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // PUT to update a user, handles FormData with JSON blob
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestPart(value = "user") String userJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication,
            HttpServletRequest request
    ) {
        try {
            // Parse the JSON string to User object
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            User userToUpdate = mapper.readValue(userJson, User.class);
            
            // DEBUG: Log incoming data
            System.out.println("🔍 Received user update request for ID: " + id);
            System.out.println("📊 Property Type: " + userToUpdate.getPropertyType());
            System.out.println("📏 Plot Size Marla: " + userToUpdate.getPlotSizeMarla());
            System.out.println("🏪 No Of Shops: " + userToUpdate.getNoOfShops());
            System.out.println("📋 Raw JSON: " + userJson);
            
            // Get current user to check if email is changing
            String currentUserEmail = authentication.getName();
            User currentUser = userService.findById(id).orElse(null);
            boolean emailChanged = currentUser != null && !currentUser.getEmail().equals(userToUpdate.getEmail());

            // Handle profile picture upload
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String filePath = fileStorageService.storeFile(profilePicture, "profiles");
                userToUpdate.setProfilePicturePath(filePath);
            }

            User updatedUser = userService.updateUser(id, userToUpdate);
            
            // If email changed and it's the current user updating their own profile, invalidate session
            if (emailChanged && currentUser != null && currentUser.getEmail().equals(currentUserEmail)) {
                // Invalidate the session so user needs to log in with new email
                request.getSession().invalidate();
            }
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}