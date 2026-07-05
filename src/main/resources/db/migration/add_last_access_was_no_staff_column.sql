-- Migration script to add last_access_was_no_staff column to users table
-- This column tracks whether the user's last portal access was granted due to "no staff" bypass

ALTER TABLE users ADD COLUMN last_access_was_no_staff BOOLEAN NOT NULL DEFAULT FALSE;

-- Add a comment to the column for documentation
ALTER TABLE users ALTER COLUMN last_access_was_no_staff COMMENT 'Tracks if user\'s last access was granted via no-staff bypass';

-- Update all existing users to have FALSE for this field (default behavior)
UPDATE users SET last_access_was_no_staff = FALSE WHERE last_access_was_no_staff IS NULL;
