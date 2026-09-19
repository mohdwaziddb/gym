package com.backend.core.gymcommon;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * GymCommonConfig - Central catalog DB (gymcommon) for gym_code -> db_name mapping.
 * Single DataSource for gymcommon, used by GymRegistryService to resolve tenant.
 */
@Configuration
public class GymCommonConfig {

    @Value("${microservice.local.database.port:3306}")
    private String localDbPort;

    @Value("${microservice.local.database.username:root}")
    private String username;

    @Value("${microservice.local.database.password:hrhk}")
    private String password;

    @Bean(name = "gymcommonDataSource")
    public DataSource gymcommonDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:" + localDbPort + "/gymcommon?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(5);
        config.setPoolName("gymcommon");
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        return new HikariDataSource(config);
    }

    @Bean(name = "gymcommonJdbcTemplate")
    public JdbcTemplate gymcommonJdbcTemplate() {
        return new JdbcTemplate(gymcommonDataSource());
    }
}
