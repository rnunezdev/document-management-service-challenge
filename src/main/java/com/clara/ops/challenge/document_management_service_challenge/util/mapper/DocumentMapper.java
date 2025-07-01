package com.clara.ops.challenge.document_management_service_challenge.util.mapper;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDto;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;

public class DocumentMapper {
  public static DocumentDto toDto(Document document) {

    return new DocumentDto(
        document.getId(),
        document.getUserId(),
        document.getDocumentName(),
        document.getTags(),
        document.getFileSize(),
        document.getFileType(),
        document.getCreatedAt());
  }
}
