package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@TestConfiguration
public class TestMinioConfig {

    @Bean
    public MinioClient minioClient(Environment env) {
        return MinioClient.builder()
                .endpoint(env.getProperty("minio.url"))
                .credentials(
                        env.getProperty("minio.access-key"),
                        env.getProperty("minio.secret-key")
                )
                .build();
    }

    @Bean
    public String minioBucket(Environment env) {
        return env.getProperty("minio.bucket", "document-bucket");
    }
}
