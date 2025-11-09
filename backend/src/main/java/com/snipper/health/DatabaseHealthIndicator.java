package com.snipper.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for database connectivity
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // Execute a simple query to check database connectivity
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result != null && result == 1) {
                return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Connected")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Query returned unexpected result")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "MySQL")
                .withDetail("status", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
