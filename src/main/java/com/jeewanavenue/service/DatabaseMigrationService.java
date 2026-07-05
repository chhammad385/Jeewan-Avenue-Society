package com.jeewanavenue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        System.out.println("🔄 Starting Database Migration Check...");
        
        try {
            // Migration 1: Add account_balance column to users table
            migrateAccountBalance();
            
            // Migration 2: Create account_transactions table
            migrateAccountTransactionsTable();
            
            // Migration 3: Add built_status and blood_group columns to users table
            migrateBuiltStatusAndBloodGroup();
            
            // Migration 4: Add built_status column to monthly_payments table
            migrateBuiltStatusToMonthlyPayments();
            
            // Migration 5: Add is_charity column to financials table
            migrateIsCharityToFinancials();
            
            System.out.println("✅ Database Migration Check Completed Successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database Migration Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migration: Add account_balance column to users table if it doesn't exist
     */
    private void migrateAccountBalance() {
        System.out.println("🔍 Checking if account_balance column exists in users table...");
        
        try {
            // Check if the column exists
            boolean columnExists = checkIfColumnExists("users", "account_balance");
            
            if (!columnExists) {
                System.out.println("➕ Adding account_balance column to users table...");
                
                // Add the column
                String addColumnSql = "ALTER TABLE users ADD COLUMN account_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00";
                jdbcTemplate.execute(addColumnSql);
                
                System.out.println("✅ Successfully added account_balance column to users table");
                
                // Optional: Initialize existing users' balances based on existing data
                initializeExistingUserBalances();
                
            } else {
                System.out.println("✅ account_balance column already exists in users table");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during account_balance migration: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if a specific column exists in a table
     */
    private boolean checkIfColumnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, tableName, columnName);
            return !result.isEmpty();
            
        } catch (Exception e) {
            System.err.println("⚠️ Error checking column existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initialize existing users' account balances based on current data
     * This is optional and can be run once during migration
     */
    private void initializeExistingUserBalances() {
        System.out.println("🔄 Initializing existing users' account balances...");
        
        try {
            // Check if we have existing data to migrate
            String checkDataSql = "SELECT COUNT(*) FROM users WHERE account_balance = 0.00";
            Integer usersWithZeroBalance = jdbcTemplate.queryForObject(checkDataSql, Integer.class);
            
            if (usersWithZeroBalance != null && usersWithZeroBalance > 0) {
                System.out.println("📊 Found " + usersWithZeroBalance + " users with zero balance. Calculating balances...");
                
                // Calculate and update account balances for existing users
                String updateBalancesSql = """
                    UPDATE users u 
                    SET account_balance = (
                        -- Total dues from dues table
                        COALESCE((SELECT SUM(due_amount) FROM dues WHERE user_id = u.id), 0)
                        -- Minus total income payments from financial records
                        - COALESCE((SELECT SUM(amount) FROM financials WHERE type = 'Income' AND plot_no = u.plot_no), 0)
                        -- Plus unpaid custom dues
                        + COALESCE((SELECT SUM(amount) FROM custom_dues WHERE user_id = u.id AND is_paid = false), 0)
                    )
                    WHERE account_balance = 0.00
                """;
                
                int updatedRows = jdbcTemplate.update(updateBalancesSql);
                System.out.println("✅ Updated account balances for " + updatedRows + " existing users");
                
                // Log some sample balances for verification
                logSampleBalances();
                
            } else {
                System.out.println("ℹ️ No users with zero balance found. Skipping balance initialization.");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error initializing user balances (non-critical): " + e.getMessage());
            // This is non-critical, so we don't throw the exception
        }
    }

    /**
     * Log some sample account balances for verification
     */
    private void logSampleBalances() {
        try {
            String sampleSql = "SELECT owner_name, plot_no, account_balance FROM users WHERE account_balance != 0.00 LIMIT 5";
            List<Map<String, Object>> samples = jdbcTemplate.queryForList(sampleSql);
            
            if (!samples.isEmpty()) {
                System.out.println("📋 Sample account balances:");
                for (Map<String, Object> row : samples) {
                    String name = (String) row.get("owner_name");
                    String plot = (String) row.get("plot_no");
                    Object balance = row.get("account_balance");
                    System.out.println("  • " + name + " (Plot: " + plot + ") - Balance: PKR " + balance);
                }
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error logging sample balances: " + e.getMessage());
        }
    }

    /**
     * Migration 2: Create account_transactions table
     */
    private void migrateAccountTransactionsTable() {
        System.out.println("🔄 Checking account_transactions table...");
        
        try {
            // Check if table exists
            String checkTableSql = """
                SELECT COUNT(*) 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME = 'account_transactions'
                """;
            
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableCount == null || tableCount == 0) {
                System.out.println("📋 Creating account_transactions table...");
                
                String createTableSql = """
                    CREATE TABLE account_transactions (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        amount DECIMAL(10,2) NOT NULL,
                        transaction_type VARCHAR(50) NOT NULL,
                        description VARCHAR(500) NOT NULL,
                        reference_id BIGINT,
                        transaction_date DATETIME NOT NULL,
                        INDEX idx_user_id (user_id),
                        INDEX idx_transaction_date (transaction_date),
                        INDEX idx_transaction_type (transaction_type),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;
                
                jdbcTemplate.execute(createTableSql);
                System.out.println("✅ account_transactions table created successfully!");
                
            } else {
                System.out.println("✅ account_transactions table already exists");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error creating account_transactions table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migration 3: Add built_status and blood_group columns to users table
     */
    private void migrateBuiltStatusAndBloodGroup() {
        System.out.println("🔄 Checking built_status and blood_group columns in users table...");
        
        try {
            // Check and add built_status column
            boolean builtStatusExists = checkIfColumnExists("users", "built_status");
            
            if (!builtStatusExists) {
                System.out.println("➕ Adding built_status column to users table...");
                String addBuiltStatusSql = "ALTER TABLE users ADD COLUMN built_status VARCHAR(50) DEFAULT NULL COMMENT 'Construction status of the property'";
                jdbcTemplate.execute(addBuiltStatusSql);
                System.out.println("✅ Successfully added built_status column to users table");
            } else {
                System.out.println("✅ built_status column already exists in users table");
            }
            
            // Check and add blood_group column
            boolean bloodGroupExists = checkIfColumnExists("users", "blood_group");
            
            if (!bloodGroupExists) {
                System.out.println("➕ Adding blood_group column to users table...");
                String addBloodGroupSql = "ALTER TABLE users ADD COLUMN blood_group VARCHAR(10) DEFAULT NULL COMMENT 'Blood group of the user'";
                jdbcTemplate.execute(addBloodGroupSql);
                System.out.println("✅ Successfully added blood_group column to users table");
            } else {
                System.out.println("✅ blood_group column already exists in users table");
            }
            
            System.out.println("✅ Built status and blood group migration completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during built_status and blood_group migration: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Migration 4: Add built_status column to monthly_payments table
     */
    private void migrateBuiltStatusToMonthlyPayments() {
        System.out.println("🔄 Checking built_status column in monthly_payments table...");
        
        try {
            // Check and add built_status column to monthly_payments table
            boolean builtStatusExists = checkIfColumnExists("monthly_payments", "built_status");
            
            if (!builtStatusExists) {
                System.out.println("➕ Adding built_status column to monthly_payments table...");
                String addBuiltStatusSql = "ALTER TABLE monthly_payments ADD COLUMN built_status VARCHAR(50) DEFAULT NULL COMMENT 'Built status filter for targeted payments'";
                jdbcTemplate.execute(addBuiltStatusSql);
                System.out.println("✅ Successfully added built_status column to monthly_payments table");
            } else {
                System.out.println("✅ built_status column already exists in monthly_payments table");
            }
            
            System.out.println("✅ Built status migration for monthly payments completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during built_status migration for monthly_payments: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Migration 5: Add is_charity column to financials table
     */
    private void migrateIsCharityToFinancials() {
        System.out.println("🔄 Checking is_charity column in financials table...");
        
        try {
            // Check and add is_charity column to financials table
            boolean isCharityExists = checkIfColumnExists("financials", "is_charity");
            
            if (!isCharityExists) {
                System.out.println("➕ Adding is_charity column to financials table...");
                String addIsCharitySql = "ALTER TABLE financials ADD COLUMN is_charity TINYINT(1) DEFAULT FALSE COMMENT 'Flag to indicate if this is a charity donation'";
                jdbcTemplate.execute(addIsCharitySql);
                System.out.println("✅ Successfully added is_charity column to financials table");
            } else {
                System.out.println("✅ is_charity column already exists in financials table");
            }
            
            System.out.println("✅ Charity flag migration for financials completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during is_charity migration for financials: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Future migration method template
     * Add new migrations here as needed
     */
    // private void migrateFutureFeature() {
    //     System.out.println("🔄 Running future migration...");
    //     // Add migration logic here
    // }
}