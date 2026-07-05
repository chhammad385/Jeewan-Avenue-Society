-- Create masjid_settings table for storing Masjid configuration
CREATE TABLE IF NOT EXISTS masjid_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default secret code (change this after first login)
INSERT INTO masjid_settings (setting_key, setting_value, updated_by) 
VALUES ('MASJID_SECRET_CODE', '1234', 'System') 
ON DUPLICATE KEY UPDATE setting_key = setting_key;
