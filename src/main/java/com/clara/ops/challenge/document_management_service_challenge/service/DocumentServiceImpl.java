package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentStatus;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService{

    @Autowired
    private ObjectMapper objectMapper;
    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public void uploadDocument(DocumentUploadRequestDTO dto){
        MultipartFile file = dto.getFile();

        String documentName = UUID.randomUUID() + "_" + dto.getDocumentName();
        String objectName = dto.getUser() + "/" + documentName;

        try (InputStream inputStream = dto.getFile().getInputStream()) {
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, dto.getFile().getSize(), -1)
                            .contentType(dto.getFile().getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
        log.info("✅ Document succesfully created in : '{}' with name: '{}' ", bucketName, objectName);

        Document document = document = Document.builder()
                        .userId(dto.getUser())
                        .documentName(documentName)
                        .tags(dto.getTags())
                        .fileSize(file.getSize())
                        .fileType(file.getContentType())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy(dto.getUser())
                        .updatedBy(dto.getUser())
                        .minioPath(objectName)
                        .status(DocumentStatus.ACTIVE.name())
                        .build();

        documentRepository.save(document);
    }

    private PGobject toJsonb(Object value) {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(objectMapper.writeValueAsString(value));
            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSONB", e);
        }
    }

}
