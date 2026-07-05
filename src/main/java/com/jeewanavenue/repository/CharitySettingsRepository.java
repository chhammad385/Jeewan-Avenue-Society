package com.jeewanavenue.repository;

import com.jeewanavenue.entity.CharitySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharitySettingsRepository extends JpaRepository<CharitySettings, Long> {
    
    Optional<CharitySettings> findBySettingKey(String settingKey);
    
    boolean existsBySettingKey(String settingKey);
}
