package com.jeewanavenue.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeewanavenue.entity.User;
import com.jeewanavenue.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("ownerName", user.getOwnerName());
            userData.put("role", user.getRole());
            userData.put("roleNormalized", user.getRole().toUpperCase().replace("-", "_"));
            return userData;
        }).collect(Collectors.toList());
    }

    @GetMapping("/login-test")
    public Map<String, Object> testLoginFormats() {
        Map<String, Object> result = new HashMap<>();
        
        String[] formRoles = {"society-member", "president", "vice-president", "general-secretary", "finance-secretary", "information-secretary"};
        Map<String, String> normalizedRoles = new HashMap<>();
        
        for (String role : formRoles) {
            normalizedRoles.put(role, role.toUpperCase().replace("-", "_"));
        }
        
        result.put("formRoles", formRoles);
        result.put("normalizedRoles", normalizedRoles);
        
        return result;
    }
}
