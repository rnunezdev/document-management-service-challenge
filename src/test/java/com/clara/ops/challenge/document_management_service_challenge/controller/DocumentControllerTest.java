package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.dto.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SuppressWarnings("removal")
@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private DocumentService documentService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldUploadDocumentSuccessfully() throws Exception {
    DocumentUploadRequestDTO metadata =
        DocumentUploadRequestDTO.builder()
            .user("test-user")
            .tags(List.of("invoice", "2025"))
            .documentName("test.pdf")
            .build();

    MockMultipartFile metadataPart =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(metadata));

    MockMultipartFile filePart =
        new MockMultipartFile(
            "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "Dummy content".getBytes());

    doNothing().when(documentService).uploadDocument(any(DocumentUploadRequestDTO.class));

    mockMvc
        .perform(multipart("/api/documents").file(metadataPart).file(filePart))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldReturnPaginatedDocuments() throws Exception {
    List<DocumentDto> docs =
        List.of(
            new DocumentDto(
                1L, "test-user", "doc.pdf", List.of("a"), 123L, "application/pdf", null));
    Metadata metadata = new Metadata(0, 10, 1, 1, 1);
    PaginatedDocumentSearch response = new PaginatedDocumentSearch(metadata, docs);

    when(documentService.searchDocumentsWithMetadata(any(DocumentSearchCriteria.class)))
        .thenReturn(response);

    MvcResult result =
        mockMvc
            .perform(
                get("/api/documents/search")
                    .param("userId", "test-user")
                    .param("documentName", "doc")
                    .param("tags", "a", "b")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn();

    String responseJson = result.getResponse().getContentAsString();

    PaginatedDocumentSearch parsed =
        objectMapper.readValue(responseJson, PaginatedDocumentSearch.class);

    Assertions.assertEquals(1L, parsed.metadata().totalItems());
    Assertions.assertEquals(10, parsed.metadata().itemsPerPage());
    Assertions.assertEquals(1, parsed.metadata().totalPages());
  }

  @Test
  void shouldFailUploadWithInvalidFileType() throws Exception {
    DocumentUploadRequestDTO metadata =
        DocumentUploadRequestDTO.builder()
            .user("test-user")
            .tags(List.of("tag1"))
            .documentName("test.pdf")
            .build();

    MockMultipartFile metadataPart =
        new MockMultipartFile(
            "metadata",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(metadata));

    MockMultipartFile invalidFilePart =
        new MockMultipartFile(
            "file",
            "malware.exe",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "bad content".getBytes());

    mockMvc
        .perform(multipart("/api/documents").file(metadataPart).file(invalidFilePart))
        .andExpect(status().isBadRequest());
  }
}
