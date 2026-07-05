package com.jeewanavenue.controller;

import com.jeewanavenue.service.DatabaseMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/database")
public class DatabaseMigrationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseMigrationService migrationService;

    /**
     * Check database migration status
     */
    @GetMapping("/migration-status")
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Check if account_balance column exists
            boolean accountBalanceExists = checkIfColumnExists("users", "account_balance");
            status.put("account_balance_exists", accountBalanceExists);
            
            if (accountBalanceExists) {
                // Get count of users with non-zero balances
                String balanceCountSql = "SELECT COUNT(*) FROM users WHERE account_balance != 0.00";
                Integer usersWithBalance = jdbcTemplate.queryForObject(balanceCountSql, Integer.class);
                status.put("users_with_balance", usersWithBalance);
                
                // Get total users count
                String totalUsersSql = "SELECT COUNT(*) FROM users";
                Integer totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Integer.class);
                status.put("total_users", totalUsers);
            }
            
            status.put("migration_complete", accountBalanceExists);
            status.put("success", true);
            
        } catch (Exception e) {
            status.put("success", false);
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Manually trigger database migration
     */
    @PostMapping("/run-migration")
    public ResponseEntity<Map<String, Object>> runMigration() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🔧 Manual migration trigger requested...");
            migrationService.runMigrations();
            
            result.put("success", true);
            result.put("message", "Database migration completed successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            System.err.println("❌ Manual migration failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get database schema information
     */
    @GetMapping("/schema-info")
    public ResponseEntity<Map<String, Object>> getSchemaInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Get users table columns
            String columnsSql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
                               "FROM INFORMATION_SCHEMA.COLUMNS " +
                               "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' " +
                               "ORDER BY ORDINAL_POSITION";
            
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(columnsSql);
            info.put("users_table_columns", columns);
            
            // Check for key tables
            String tablesSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                              "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN " +
                              "('users', 'dues', 'financials', 'custom_dues', 'monthly_payments')";
            
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(tablesSql);
            info.put("existing_tables", tables);
            
            info.put("success", true);
            
        } catch (Exception e) {
            info.put("success", false);
            info.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(info);
    }

    /**
     * Get sample account balances for verification
     */
    @GetMapping("/sample-balances")
    public ResponseEntity<Map<String, Object>> getSampleBalances() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if account_balance column exists first
            boolean columnExists = checkIfColumnExists("users", "account_balance");
            
            if (!columnExists) {
                result.put("success", false);
                result.put("error", "account_balance column does not exist. Run migration first.");
                return ResponseEntity.ok(result);
            }
            
            // Get sample balances
            String sampleSql = "SELECT owner_name, plot_no, account_balance FROM users " +
                             "WHERE plot_no IS NOT NULL ORDER BY account_balance DESC LIMIT 10";
            
            List<Map<String, Object>> samples = jdbcTemplate.queryForList(sampleSql);
            result.put("sample_balances", samples);
            
            // Get balance statistics
            String statsSql = "SELECT " +
                            "COUNT(*) as total_users, " +
                            "COUNT(CASE WHEN account_balance > 0 THEN 1 END) as users_with_due, " +
                            "COUNT(CASE WHEN account_balance < 0 THEN 1 END) as users_with_debit, " +
                            "COUNT(CASE WHEN account_balance = 0 THEN 1 END) as users_with_zero_balance, " +
                            "SUM(CASE WHEN account_balance > 0 THEN account_balance ELSE 0 END) as total_due_amount, " +
                            "SUM(CASE WHEN account_balance < 0 THEN ABS(account_balance) ELSE 0 END) as total_debit_amount " +
                            "FROM users WHERE plot_no IS NOT NULL";
            
            Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
            result.put("balance_statistics", stats);
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Helper method to check if column exists
     */
    private boolean checkIfColumnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, tableName, columnName);
            return !result.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
}