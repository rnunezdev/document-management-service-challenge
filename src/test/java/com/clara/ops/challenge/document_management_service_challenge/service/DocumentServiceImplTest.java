package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentStatus;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentSearchRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {

  @Mock private ObjectProvider<MinioClient> minioClientProvider;

  @Mock private MinioClient minioClient;

  @Mock private DocumentRepository documentRepository;

  @Mock private DocumentSearchRepository documentSearchRepository;

  @Mock private MultipartFile multipartFile;

  @InjectMocks private DocumentServiceImpl documentService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(documentService, "bucketName", "test-bucket");
  }

  @Test
  void testUploadDocumentSuccess() throws Exception {

    DocumentUploadRequestDTO dto =
        new DocumentUploadRequestDTO("maria", "resume.pdf", List.of("job", "cv"), multipartFile);
    when(minioClientProvider.getObject()).thenReturn(minioClient);
    when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));
    when(multipartFile.getSize()).thenReturn(10L);
    when(multipartFile.getContentType()).thenReturn("application/pdf");

    documentService.uploadDocument(dto);

    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    verify(documentRepository, times(1)).save(any(Document.class));
  }

  @Test
  void testUploadDocumentNullUserShouldThrow() {
    DocumentUploadRequestDTO dto =
        new DocumentUploadRequestDTO(null, "file.pdf", List.of("tag"), multipartFile);

    assertThrows(RuntimeException.class, () -> documentService.uploadDocument(dto));
  }

  @Test
  void testSearchDocumentsWithMetadataSuccess() {
    Document doc =
        Document.builder()
            .id(1L)
            .userId("john")
            .documentName("file.pdf")
            .tags(List.of("x"))
            .fileSize(10L)
            .fileType("pdf")
            .status(DocumentStatus.ACTIVE.name())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    DocumentSearchCriteria criteria = new DocumentSearchCriteria("john", null, null, 1, 5);

    when(documentSearchRepository.searchDocuments(criteria)).thenReturn(List.of(doc));
    when(documentSearchRepository.countDocuments(criteria)).thenReturn(1L);

    PaginatedDocumentSearch result = documentService.searchDocumentsWithMetadata(criteria);

    assertThat(result.documents()).hasSize(1);
    assertThat(result.metadata().totalItems()).isEqualTo(1);
    assertThat(result.metadata().currentItems()).isEqualTo(1);
  }

  @Test
  void testSearchDocumentsWithMetadataEmptyResult() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria("no-user", null, null, 1, 5);

    when(documentSearchRepository.searchDocuments(criteria)).thenReturn(List.of());
    when(documentSearchRepository.countDocuments(criteria)).thenReturn(0L);

    PaginatedDocumentSearch result = documentService.searchDocumentsWithMetadata(criteria);

    assertThat(result.documents()).isEmpty();
    assertThat(result.metadata().totalItems()).isEqualTo(0);
  }

  @Test
  void testUploadDocumentMinIOFailureShouldThrow() throws Exception {
    DocumentUploadRequestDTO dto =
        new DocumentUploadRequestDTO("maria", "fail.pdf", List.of("fail"), multipartFile);

    when(multipartFile.getInputStream()).thenThrow(new IOException("Fail"));

    assertThrows(RuntimeException.class, () -> documentService.uploadDocument(dto));
  }
}
