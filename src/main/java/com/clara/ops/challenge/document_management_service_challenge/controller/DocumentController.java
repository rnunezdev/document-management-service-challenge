package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.clara.ops.challenge.document_management_service_challenge.util.validator.PdfFileOnly;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = " API for big size document management")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;


    @Operation(summary = "Upload a document",
            description = "Uploads a document along with metadata (document name, tags, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadDocument(
            @Parameter(description = "Document metadata as JSON", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DocumentUploadRequestDTO.class)))
            @RequestPart("metadata") @Valid DocumentUploadRequestDTO metadata,

            @Parameter(description = "The file to upload", required = true)
            @PdfFileOnly
            @RequestPart("file") MultipartFile file
    ) {
        log.info("Request for uploading document arrived");

        metadata.setFile(file);
        documentService.uploadDocument(metadata);
    }


    @Operation(
            summary = "Search documents with optional filters",
            description = "Search documents by user ID, document name, and tags. Returns paginated results ordered by creation date descending."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated documents"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public PaginatedDocumentSearch searchDocuments(
            @Parameter(description = "User ID to filter by") @RequestParam(required = false) String userId,
            @Parameter(description = "Partial document name to search") @RequestParam(required = false) String documentName,
            @Parameter(description = "List of tags to filter by") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "Page number (starting from 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size
    ) {
        DocumentSearchCriteria criteria = new DocumentSearchCriteria(userId, documentName, tags, page, size);
        return documentService.searchDocumentsWithMetadata(criteria);
    }

}
