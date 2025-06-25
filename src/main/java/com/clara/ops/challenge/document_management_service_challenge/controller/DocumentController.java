package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = " API for big size document management")
public class DocumentController {

    private final DocumentServiceImpl documentService;


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
            @RequestPart("file") MultipartFile file
    ) {
        metadata.setFile(file);
        documentService.uploadDocument(metadata);
    }
}
