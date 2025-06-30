package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.clara.ops.challenge.document_management_service_challenge.config.AbstractMinioPostgresContainerTest;
import com.clara.ops.challenge.document_management_service_challenge.config.TestMinioConfig;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import java.io.IOException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestMinioConfig.class)
public class DocumentControllerIT extends AbstractMinioPostgresContainerTest {

  @LocalServerPort private int port;

  @Autowired private DocumentRepository documentRepository;

  private WebTestClient webTestClient;

  @Autowired private MinioClient minioClient;

  @Autowired private String minioBucket;

  @Resource private DataSource dataSource;

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

  @BeforeEach
  void setUp() {
    documentRepository.deleteAll();
    this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @BeforeEach
  void ensureMinioBucketExists() throws Exception {
    boolean exists =
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
    }
  }

  @Test
  void shouldUploadAndSearchDocumentSuccessfully() throws IOException {
    String userId = "raul";
    String documentName = "sample.pdf";

    ClassPathResource resource = new ClassPathResource("test-files/sample.pdf");
    assertThat(resource.exists()).isTrue();

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add(
        "metadata",
        new HttpEntity<>(
            """
            {
              "user": "raul",
              "documentName": "sample.pdf",
              "tags": ["contract", "legal"]
            }
            """,
            new HttpHeaders() {
              {
                setContentType(MediaType.APPLICATION_JSON);
              }
            }));
    parts.add("file", resource);

    webTestClient
        .post()
        .uri("/api/documents")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(parts))
        .exchange()
        .expectStatus()
        .isCreated();

    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/api/documents/search")
                    .queryParam("userId", userId)
                    .queryParam("documentName", documentName)
                    .queryParam("tags", "contract")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(PaginatedDocumentSearch.class)
        .value(
            response -> {
              assertThat(response.documents()).hasSize(1);
              DocumentDto doc = response.documents().get(0);
              assertThat(doc.name()).contains("sample");

              Metadata metadata = response.metadata();
              assertThat(metadata.totalItems()).isEqualTo(1);
              assertThat(metadata.itemsPerPage()).isEqualTo(10);
              assertThat(metadata.totalPages()).isEqualTo(1);
            });
  }
}
