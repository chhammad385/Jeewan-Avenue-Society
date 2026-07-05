-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT INTO categories (name, description) VALUES
('Committee Member', 'Members of the management committee'),
('Security Guard', 'Security personnel for the society'),
('Gardener', 'Gardening and landscaping staff'),
('Imam-Masjid', 'Mosque imam and religious staff')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Add index for faster category lookups
CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
