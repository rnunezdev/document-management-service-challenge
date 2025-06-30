package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record DocumentDto(
    Long id,
    String user,
    String name,
    List<String> tags,
    Long size,
    String type,
    LocalDateTime createdAt) {}
