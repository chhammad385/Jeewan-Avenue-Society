package com.jeewanavenue.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String email = authentication.getName(); // Authentication uses email as username
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            User user = userOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("ownerName", user.getOwnerName());
            userData.put("id", user.getId());

            return ResponseEntity.ok(userData);
            
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/check-permission")
    public ResponseEntity<Map<String, Boolean>> checkPermission(@RequestParam String permission) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String email = authentication.getName(); // Authentication uses email as username
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            User user = userOpt.get();
            boolean hasPermission = checkUserPermission(user.getRole(), permission);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("Error checking permission: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    private boolean checkUserPermission(String role, String permission) {
        // Normalize the role to match the Spring Security format
        String normalizedRole = role.toUpperCase().replace("-", "_").replace(" ", "_");
        
        switch (permission.toUpperCase()) {
            case "PLOTS_EDIT":
                return normalizedRole.equals("PRESIDENT") || normalizedRole.equals("GENERAL_SECRETARY");
            
            case "MANAGE_MEMBERS_EDIT":
                return normalizedRole.equals("PRESIDENT") || normalizedRole.equals("GENERAL_SECRETARY");
            
            case "MANAGE_USERS_EDIT":
                return normalizedRole.equals("PRESIDENT") || normalizedRole.equals("GENERAL_SECRETARY");
            
            // Financial operations - Finance Secretary ONLY (President removed per requirements)
            case "FINANCIALS_ADD":
            case "FINANCIALS_EDIT":
            case "FINANCIALS_DELETE":
                return normalizedRole.equals("FINANCE_SECRETARY");
            
            // Announcement operations - Information Secretary ONLY (President removed per requirements)
            case "ANNOUNCEMENTS_UPLOAD":
            case "ANNOUNCEMENTS_CREATE":
            case "ANNOUNCEMENTS_EDIT":
                return normalizedRole.equals("INFORMATION_SECRETARY");
            
            // Document operations - Information Secretary ONLY (President removed per requirements)
            case "DOCUMENTS_UPLOAD":
            case "DOCUMENTS_DELETE":
                return normalizedRole.equals("INFORMATION_SECRETARY");
            
            // Renter documents - General Secretary ONLY (President removed per requirements)
            case "RENTERS_ADD":
            case "RENTERS_EDIT":
            case "RENTERS_DELETE":
                return normalizedRole.equals("GENERAL_SECRETARY");
            
            case "PRESIDENT_CONSOLE_ACCESS":
                return normalizedRole.equals("PRESIDENT") || normalizedRole.equals("GENERAL_SECRETARY") || 
                       normalizedRole.equals("FINANCE_SECRETARY") || normalizedRole.equals("INFORMATION_SECRETARY") ||
                       normalizedRole.equals("VICE_PRESIDENT");
            
            case "PRAYER_TIMER":
                return true; // Everyone can set prayer timer
            
            case "BASIC_USER_ACCESS":
                return true; // All authenticated users including society members
                
            case "READ_FINANCIALS":
            case "READ_PLOTS":
            case "READ_ANNOUNCEMENTS":
            case "READ_USERS":
            case "READ_MEMBERS":
            case "READ_RENTERS":
                return true; // All authenticated users can read basic information
            
            default:
                return false;
        }
    }
}
