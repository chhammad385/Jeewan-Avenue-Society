-- Add property_type column to monthly_payments table
-- This column stores the property type (House, Shop) for filtering users
-- This script is idempotent - safe to run multiple times

-- Check if column exists before adding it
SET @dbname = DATABASE();
SET @tablename = 'monthly_payments';
SET @columnname = 'property_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1', -- Column exists, do nothing
  'ALTER TABLE monthly_payments ADD COLUMN property_type VARCHAR(50) AFTER created_by' -- Add column
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Update existing records to have a default property type (only if column was just added)
UPDATE monthly_payments 
SET property_type = 'House' 
WHERE property_type IS NULL;
