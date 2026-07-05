-- Add built_status column to monthly_payments table
-- This allows filtering monthly payments by built status

-- Check if built_status column exists, if not add it
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'monthly_payments' 
AND COLUMN_NAME = 'built_status';

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE monthly_payments ADD COLUMN built_status VARCHAR(50) DEFAULT NULL COMMENT ''Built status filter for targeted payments''',
    'SELECT ''Column built_status already exists in monthly_payments'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the column was added
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'monthly_payments' 
AND COLUMN_NAME = 'built_status';