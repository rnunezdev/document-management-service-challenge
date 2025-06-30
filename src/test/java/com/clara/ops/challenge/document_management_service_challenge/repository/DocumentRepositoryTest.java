package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.config.AbstractPostgresContainerTest;
import com.clara.ops.challenge.document_management_service_challenge.config.TestMinioConfig;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentStatus;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestMinioConfig.class)
@ActiveProfiles("test")
public class DocumentRepositoryTest extends AbstractPostgresContainerTest {

    @Resource
    private DocumentRepository documentRepository;

    @BeforeEach
    void cleanUp() {
        documentRepository.deleteAll();
    }


    @Test
    void shouldSaveDocument() {
        String userId = "raul";
        Document document = buildSampleDocument(userId, "doc1.pdf");

        Document saved = documentRepository.save(document);
        Optional<Document> optionalRetrieved = documentRepository.findById(saved.getId());


        // Assert
        assertThat(optionalRetrieved).isPresent();
        Document retrieved = optionalRetrieved.get();

        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getUserId()).isEqualTo("raul");
        assertThat(retrieved.getDocumentName()).isEqualTo("doc1.pdf");
        assertThat(retrieved.getFileSize()).isEqualTo(1234L);
        assertThat(retrieved.getFileType()).isEqualTo("application/pdf");
        assertThat(retrieved.getMinioPath()).isEqualTo("document-bucket/doc1.pdf");
        assertThat(retrieved.getCreatedBy()).isEqualTo("raul");
        assertThat(retrieved.getUpdatedBy()).isEqualTo("raul");
        assertThat(retrieved.getStatus()).isEqualTo(DocumentStatus.ACTIVE.name());

    }

    private Document buildSampleDocument(String userId, String name) {
        return buildSampleDocument(userId, name, DocumentStatus.ACTIVE.name());
    }

    private Document buildSampleDocument(String userId, String name, String status) {
        return Document.builder()
                .userId(userId)
                .documentName(name)
                .tags(List.of("test"))
                .fileSize(1234L)
                .fileType("application/pdf")
                .createdAt(LocalDateTime.now().withNano(0))
                .updatedAt(LocalDateTime.now().withNano(0))
                .createdBy(userId)
                .updatedBy(userId)
                .minioPath("document-bucket/" + name)
                .status(status)
                .build();
    }
}

