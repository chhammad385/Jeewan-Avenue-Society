-- Add built_status and blood_group columns to users table
-- This migration adds two new columns for enhanced user profile management

-- Check if built_status column exists, if not add it
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'built_status';

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN built_status VARCHAR(50) DEFAULT NULL COMMENT ''Construction status of the property''',
    'SELECT ''Column built_status already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if blood_group column exists, if not add it
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'blood_group';

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN blood_group VARCHAR(10) DEFAULT NULL COMMENT ''Blood group of the user''',
    'SELECT ''Column blood_group already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the columns were added
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'users' 
AND COLUMN_NAME IN ('built_status', 'blood_group')
ORDER BY COLUMN_NAME;