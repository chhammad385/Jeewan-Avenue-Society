package com.jeewanavenue.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charity_settings")
public class CharitySettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "setting_key", unique = true, nullable = false)
    private String settingKey;
    
    @Column(name = "setting_value", nullable = false)
    private String settingValue;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public CharitySettings() {
    }
    
    public CharitySettings(String settingKey, String settingValue, String updatedBy) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.updatedBy = updatedBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSettingKey() {
        return settingKey;
    }
    
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }
    
    public String getSettingValue() {
        return settingValue;
    }
    
    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
