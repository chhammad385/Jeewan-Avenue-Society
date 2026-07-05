-- Add resident status columns to plots table
ALTER TABLE plots ADD COLUMN resident_owner BOOLEAN DEFAULT FALSE;
ALTER TABLE plots ADD COLUMN resident_renter BOOLEAN DEFAULT FALSE;

-- Update existing records to have default values
UPDATE plots SET resident_owner = FALSE WHERE resident_owner IS NULL;
UPDATE plots SET resident_renter = FALSE WHERE resident_renter IS NULL;