package com.jeewanavenue.repository;

import com.jeewanavenue.entity.MasjidSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasjidSettingsRepository extends JpaRepository<MasjidSettings, Long> {
    
    Optional<MasjidSettings> findBySettingKey(String settingKey);
    
    boolean existsBySettingKey(String settingKey);
}
