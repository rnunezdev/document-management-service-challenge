package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "documents", schema = "document_schema")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;

  private String documentName;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> tags;

  private String minioPath;

  private Long fileSize;

  private String fileType;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private String createdBy;

  private String updatedBy;

  private String status;
}
