package com.clara.ops.challenge.document_management_service_challenge.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentSearchCriteria;
import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DocumentSearchRepositoryTest {

  @Mock private EntityManager entityManager;

  @Mock private Query query;

  @InjectMocks private DocumentSearchRepositoryImpl repository;

  @Test
  void shouldReturnEmptyListWhenNoDocumentsFound() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria(null, null, null, 1, 10);

    when(entityManager.createNativeQuery(anyString(), eq(Document.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of());

    List<Document> result = repository.searchDocuments(criteria);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnResultsWhenMatchingCriteria() {
    DocumentSearchCriteria criteria =
        new DocumentSearchCriteria("john", "report", List.of("finance", "q2"), 2, 5);

    when(entityManager.createNativeQuery(anyString(), eq(Document.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of(new Document(), new Document(), new Document()));

    List<Document> result = repository.searchDocuments(criteria);
    assertEquals(3, result.size());
  }

  @Test
  void shouldUseCorrectPaginationOffset() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria(null, null, null, 3, 20);

    when(entityManager.createNativeQuery(anyString(), eq(Document.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of());

    repository.searchDocuments(criteria);

    verify(query).setParameter("limit", 20);
    verify(query).setParameter("offset", 40);
  }

  @Test
  void shouldHandleCountProperly() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria("admin", null, null, 1, 10);

    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(100L);

    long count = repository.countDocuments(criteria);
    assertEquals(100L, count);
  }

  @Test
  void shouldUseBlankDocumentNameProperly() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria(null, " ", null, 1, 10);

    when(entityManager.createNativeQuery(anyString(), eq(Document.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of());

    List<Document> result = repository.searchDocuments(criteria);
    assertNotNull(result);
  }

  @Test
  void shouldThrowExceptionOnEntityManagerFailure() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria(null, null, null, 1, 10);

    when(entityManager.createNativeQuery(anyString(), eq(Document.class)))
        .thenThrow(RuntimeException.class);

    assertThrows(RuntimeException.class, () -> repository.searchDocuments(criteria));
  }

  @Test
  void shouldThrowExceptionOnCountFailure() {
    DocumentSearchCriteria criteria = new DocumentSearchCriteria(null, null, null, 1, 10);

    when(entityManager.createNativeQuery(anyString())).thenThrow(RuntimeException.class);

    assertThrows(RuntimeException.class, () -> repository.countDocuments(criteria));
  }
}
