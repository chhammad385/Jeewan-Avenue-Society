package com.jeewanavenue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationStartedEvent.class)
    @Order(1) // Run before migration service
    public void initializeDatabase() {
        System.out.println("🗄️ Initializing Database Connection...");
        
        try {
            // Test database connection
            String testQuery = "SELECT 1";
            jdbcTemplate.queryForObject(testQuery, Integer.class);
            System.out.println("✅ Database connection established successfully");
            
            // Check if users table exists
            checkUsersTable();
            
            // Run database migrations automatically
            runDatabaseMigrations();
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            System.err.println("⚠️ Please ensure your database is running and connection details are correct");
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void runDatabaseMigrations() {
        System.out.println("\n🔄 Running Database Migrations...");
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:db/migration/*.sql");
            
            if (resources.length == 0) {
                System.out.println("ℹ️ No migration scripts found");
                return;
            }
            
            // Sort migration scripts by filename to ensure consistent execution order
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
            
            int successCount = 0;
            int skipCount = 0;
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                try {
                    System.out.println("  📝 Executing migration: " + filename);
                    
                    try (Connection connection = dataSource.getConnection()) {
                        ScriptUtils.executeSqlScript(connection, resource);
                        successCount++;
                        System.out.println("    ✅ Migration completed: " + filename);
                    }
                    
                } catch (Exception e) {
                    // Check if error is about duplicate column (already exists)
                    String errorMsg = e.getMessage().toLowerCase();
                    if (errorMsg.contains("duplicate column") || 
                        errorMsg.contains("column already exists") ||
                        errorMsg.contains("already exists")) {
                        skipCount++;
                        System.out.println("    ⏭️ Migration skipped (already applied): " + filename);
                    } else {
                        System.err.println("    ⚠️ Migration warning for " + filename + ": " + e.getMessage());
                        // Don't throw exception, continue with other migrations
                    }
                }
            }
            
            System.out.println("\n✅ Database migrations completed!");
            System.out.println("   • Successfully applied: " + successCount + " migration(s)");
            if (skipCount > 0) {
                System.out.println("   • Already applied: " + skipCount + " migration(s)");
            }
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("❌ Error running migrations: " + e.getMessage());
            // Don't throw exception to allow app to continue
        }
    }

    private void checkUsersTable() {
        try {
            String checkTableSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                                  "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users'";
            
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableCount != null && tableCount > 0) {
                System.out.println("✅ Users table exists in database");
                
                // Get user count for information
                String userCountSql = "SELECT COUNT(*) FROM users";
                Integer userCount = jdbcTemplate.queryForObject(userCountSql, Integer.class);
                System.out.println("📊 Found " + userCount + " users in database");
                
            } else {
                System.err.println("⚠️ Users table does not exist!");
                System.err.println("ℹ️ Please ensure your database schema is properly set up");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error checking users table: " + e.getMessage());
            // Don't throw exception here as table might not exist yet
        }
    }
}