package com.jeewanavenue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${app.allowed-origins}")
    private String allowedOrigins;

    // Configure static resource handling for uploads
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from /uploads/** URLs
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600); // Cache for 1 hour in production
                
        // Add security headers for static resources
        registry.addResourceHandler("/css/**", "/js/**", "/image/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", "classpath:/static/image/")
                .setCachePeriod(86400); // Cache for 24 hours
    }

    // Configure CORS for development and production domains
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        // Also allow CORS for all endpoints (including static resources)
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}