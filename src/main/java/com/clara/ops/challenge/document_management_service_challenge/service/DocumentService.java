package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import org.springframework.data.domain.Page;

public interface DocumentService {
    void uploadDocument(DocumentUploadRequestDTO dto);
    PaginatedDocumentSearch searchDocumentsWithMetadata(DocumentSearchCriteria criteria);


}
