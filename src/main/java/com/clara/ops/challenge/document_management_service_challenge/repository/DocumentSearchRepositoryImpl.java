package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentSearchRepositoryImpl implements DocumentSearchRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List searchDocuments(DocumentSearchCriteria criteria) {
    StringBuilder sql = new StringBuilder("SELECT * FROM document_schema.documents d WHERE 1=1");
    Map<String, Object> parameters = new HashMap<>();

    appendFilterConditions(sql, parameters, criteria);

    sql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset");

    int page = criteria.page();
    int size = criteria.size();
    int offset = Math.max(0, (page - 1) * size);

    parameters.put("limit", size);
    parameters.put("offset", offset);

    Query query = entityManager.createNativeQuery(sql.toString(), Document.class);
    parameters.forEach(query::setParameter);
    var result = query.getResultList();
    return result;
  }

  public long countDocuments(DocumentSearchCriteria criteria) {
    StringBuilder sql =
        new StringBuilder("SELECT COUNT(*) FROM document_schema.documents d WHERE 1=1");
    Map<String, Object> parameters = new HashMap<>();

    appendFilterConditions(sql, parameters, criteria);

    Query query = entityManager.createNativeQuery(sql.toString());
    parameters.forEach(query::setParameter);

    return ((Number) query.getSingleResult()).longValue();
  }

  private void appendFilterConditions(
      StringBuilder sql, Map<String, Object> parameters, DocumentSearchCriteria criteria) {
    if (criteria.userId() != null && !criteria.userId().isBlank()) {
      sql.append(" AND d.user_id = :userId");
      parameters.put("userId", criteria.userId());
    }

    if (criteria.documentName() != null && !criteria.documentName().isBlank()) {
      sql.append(" AND d.document_name ILIKE :documentName");
      parameters.put("documentName", "%" + criteria.documentName() + "%");
    }

    if (criteria.tags() != null && !criteria.tags().isEmpty()) {
      sql.append(
          """
            AND EXISTS (
              SELECT 1 FROM jsonb_array_elements_text(d.tags) AS tag
              WHERE tag IN (:tags)
            )
          """);
      parameters.put("tags", criteria.tags());
    }
  }
}
