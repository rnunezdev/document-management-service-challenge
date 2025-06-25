package com.clara.ops.challenge.document_management_service_challenge.init;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class MinioInitializer {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            log.info("🚀 Verifying bucket existence '{}' in MinIO...", bucketName);

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("✅ Bucket '{}' created successfully.", bucketName);
            } else {
                log.info("🪣 Bucket '{}' already exists.", bucketName);
            }

        } catch (Exception e) {
            log.error("❌ Failed to initialize bucket '{}'", bucketName, e);
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }
}

