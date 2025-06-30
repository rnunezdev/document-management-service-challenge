package com.clara.ops.challenge.document_management_service_challenge.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;


@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractMinioPostgresContainerTest {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.4")
                    .withDatabaseName("challenge")
                    .withUsername("clara_user")
                    .withPassword("clara_pass");

    @Container
    public static final GenericContainer<?> MINIO_CONTAINER = new GenericContainer<>("quay.io/minio/minio:RELEASE.2024-04-18T19-09-19Z")
            .withExposedPorts(9000)
            .withCommand("server /data")
            .withEnv("MINIO_ROOT_USER", "minio")
            .withEnv("MINIO_ROOT_PASSWORD", "minio123")
            .withNetwork(Network.SHARED)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        POSTGRES_CONTAINER.start();
        MINIO_CONTAINER.start();

        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        String minioUrl = String.format("http://%s:%d",
                MINIO_CONTAINER.getHost(),
                MINIO_CONTAINER.getMappedPort(9000));

        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.access-key", () -> "minio");
        registry.add("minio.secret-key", () -> "minio123");
        registry.add("minio.bucket", () -> "documents-test");
    }

    protected abstract DataSource getDataSource();

    @BeforeAll
    void initializeSchema() throws Exception {
        try (Connection conn = getDataSource().getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-init.sql"));
        }
    }
}
