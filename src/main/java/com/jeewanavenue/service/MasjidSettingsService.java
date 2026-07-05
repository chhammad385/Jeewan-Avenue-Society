package com.jeewanavenue.service;

import com.jeewanavenue.entity.MasjidSettings;
import com.jeewanavenue.repository.MasjidSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MasjidSettingsService {
    
    private static final String SECRET_CODE_KEY = "MASJID_SECRET_CODE";
    
    @Autowired
    private MasjidSettingsRepository settingsRepository;
    
    /**
     * Get the current secret code
     */
    public Optional<String> getSecretCode() {
        return settingsRepository.findBySettingKey(SECRET_CODE_KEY)
                .map(MasjidSettings::getSettingValue);
    }
    
    /**
     * Validate the provided secret code
     */
    public boolean validateSecretCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        Optional<String> savedCode = getSecretCode();
        if (savedCode.isEmpty()) {
            return false; // No code set yet
        }
        
        return savedCode.get().equals(code.trim());
    }
    
    /**
     * Set or update the secret code
     */
    @Transactional
    public MasjidSettings setSecretCode(String newCode, String updatedBy) {
        Optional<MasjidSettings> existingSetting = settingsRepository.findBySettingKey(SECRET_CODE_KEY);
        
        if (existingSetting.isPresent()) {
            // Update existing code
            MasjidSettings setting = existingSetting.get();
            setting.setSettingValue(newCode);
            setting.setUpdatedBy(updatedBy);
            return settingsRepository.save(setting);
        } else {
            // Create new code
            MasjidSettings newSetting = new MasjidSettings(SECRET_CODE_KEY, newCode, updatedBy);
            return settingsRepository.save(newSetting);
        }
    }
    
    /**
     * Check if secret code is set
     */
    public boolean isSecretCodeSet() {
        return settingsRepository.existsBySettingKey(SECRET_CODE_KEY);
    }
}
