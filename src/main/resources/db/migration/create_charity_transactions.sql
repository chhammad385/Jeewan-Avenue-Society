-- Create charity_transactions table for separate Charity accounting system
CREATE TABLE IF NOT EXISTS charity_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    month VARCHAR(50),
    description VARCHAR(1000),
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    source VARCHAR(255),
    vendor VARCHAR(255),
    receipt_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_date (date),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
