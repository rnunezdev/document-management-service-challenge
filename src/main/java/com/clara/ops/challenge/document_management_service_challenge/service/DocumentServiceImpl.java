package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentStatus;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentSearchRepository;
import com.clara.ops.challenge.document_management_service_challenge.util.mapper.DocumentMapper;
import io.minio.*;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

  private final ObjectProvider<MinioClient> minioClientProvider;
  private final DocumentSearchRepository documentSearchRepository;
  private final DocumentRepository documentRepository;

  @Value("${minio.bucket}")
  private String bucketName;

  private static final long PART_SIZE = 5 * 1024 * 1024; // 5MB
  private static final Object lock = new Object();

  @Override
  public void uploadDocument(DocumentUploadRequestDTO dto) {
    MultipartFile file = dto.getFile();
    String documentName = UUID.randomUUID() + "_" + dto.getDocumentName();
    String objectName = dto.getUser() + "/" + documentName;

    uploadToMinio(file, objectName);

    Document document =
        Document.builder()
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

  @Override
  public PaginatedDocumentSearch searchDocumentsWithMetadata(DocumentSearchCriteria criteria) {
    Page<Document> documentPage = searchDocuments(criteria);
    return toPaginatedDocumentSearch(documentPage);
  }

  public Page<Document> searchDocuments(DocumentSearchCriteria criteria) {
    List<Document> documents = documentSearchRepository.searchDocuments(criteria);
    long total = documentSearchRepository.countDocuments(criteria);
    return new PageImpl<>(
        documents, PageRequest.of(Math.max(0, criteria.page() - 1), criteria.size()), total);
  }

  private PaginatedDocumentSearch toPaginatedDocumentSearch(Page<Document> page) {
    List<DocumentDto> documentDtos = page.getContent().stream().map(DocumentMapper::toDto).toList();

    Metadata metadata =
        new Metadata(
            page.getNumber(),
            page.getSize(),
            page.getNumberOfElements(),
            page.getTotalPages(),
            page.getTotalElements());

    return new PaginatedDocumentSearch(metadata, documentDtos);
  }

  private void uploadToMinio(MultipartFile file, String objectName) {
    try (InputStream inputStream = file.getInputStream()) {
      MinioClient minioClient = minioClientProvider.getObject();
      synchronized (lock) {
        minioClient.putObject(
            PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                    inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
      }

      log.info("Document successfully uploaded with complete stream: {}", objectName);
    } catch (Exception e) {
      log.error("Error while uploading document to MinIO. Root cause: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload to MinIO", e);
    }
  }
}
