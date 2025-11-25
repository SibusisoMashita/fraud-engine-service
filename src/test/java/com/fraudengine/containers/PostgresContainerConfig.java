package com.fraudengine.containers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Safe Testcontainers configuration that works on Windows, Mac, Linux, CI.
 * No static initializer → allows testcontainers.properties to load first.
 */
@Testcontainers
public abstract class PostgresContainerConfig {

    // Testcontainers will start automatically when first used
    public static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("frauddb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
