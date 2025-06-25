package com.clara.ops.challenge.document_management_service_challenge.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPostgresContainerTest {

    @Autowired
    private DataSource dataSource;

    @Container
    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.4")
                    .withDatabaseName("challenge")
                    .withUsername("clara_user")
                    .withPassword("clara_pass");


    @BeforeAll
    void setUpSchema() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-init.sql"));
        }
    }
    static {
        POSTGRES_CONTAINER.start();
        System.setProperty("SPRING_DATASOURCE_URL", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("SPRING_DATASOURCE_USERNAME", POSTGRES_CONTAINER.getUsername());
        System.setProperty("SPRING_DATASOURCE_PASSWORD", POSTGRES_CONTAINER.getPassword());
    }

}
