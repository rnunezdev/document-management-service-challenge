package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;
import com.clara.ops.challenge.document_management_service_challenge.dto.PaginatedDocumentSearch;

public interface DocumentService {
  void uploadDocument(DocumentUploadRequestDTO dto);

  PaginatedDocumentSearch searchDocumentsWithMetadata(DocumentSearchCriteria criteria);
}
