-- Add month column to financials table for tracking payment months
ALTER TABLE financials ADD COLUMN IF NOT EXISTS month VARCHAR(20);
