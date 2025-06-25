package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentUploadRequestDTO;

public interface DocumentService {
    void uploadDocument(DocumentUploadRequestDTO dto);

}
