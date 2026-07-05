package com.jeewanavenue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${app.allowed-origins}")
    private String allowedOrigins;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            try (Connection conn = dataSource.getConnection()) {
                health.put("database", "UP");
                health.put("database_url", conn.getMetaData().getURL());
            }
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }
        
        // Check upload directory
        File uploadDirectory = new File(uploadDir);
        if (uploadDirectory.exists() && uploadDirectory.canWrite()) {
            health.put("upload_directory", "UP");
            health.put("upload_path", uploadDirectory.getAbsolutePath());
        } else {
            health.put("upload_directory", "DOWN");
            health.put("upload_path", uploadDirectory.getAbsolutePath());
        }
        
        // Configuration info
        health.put("allowed_origins", allowedOrigins);
        health.put("java_version", System.getProperty("java.version"));
        health.put("os", System.getProperty("os.name"));
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Jeewan Avenue Backend is running successfully!");
        response.put("timestamp", new java.util.Date().toString());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
}