-- Add property type and renter details columns to users table

-- Add property_type column
ALTER TABLE users ADD COLUMN IF NOT EXISTS property_type VARCHAR(50);

-- Add no_of_shops column
ALTER TABLE users ADD COLUMN IF NOT EXISTS no_of_shops INTEGER;

-- Add renter details columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS renter_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS renter_phone_no VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS renter_cnic VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS renter_previous_address TEXT;

-- Add comments for clarity
COMMENT ON COLUMN users.property_type IS 'Type of property: House, Shop';
COMMENT ON COLUMN users.no_of_shops IS 'Number of shops if property_type is Shop';
COMMENT ON COLUMN users.renter_name IS 'Name of the renter if status is Rented or Both';
COMMENT ON COLUMN users.renter_phone_no IS 'Phone number of the renter';
COMMENT ON COLUMN users.renter_cnic IS 'CNIC of the renter';
COMMENT ON COLUMN users.renter_previous_address IS 'Previous address of the renter';
