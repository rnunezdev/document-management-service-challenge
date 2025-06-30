package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import java.util.List;

public interface DocumentSearchRepository {
  List<Document> searchDocuments(DocumentSearchCriteria criteria);

  long countDocuments(DocumentSearchCriteria criteria);
}
