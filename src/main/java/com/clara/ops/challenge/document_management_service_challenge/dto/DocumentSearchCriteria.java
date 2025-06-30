package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.List;

public record DocumentSearchCriteria(
    String userId, String documentName, List<String> tags, int page, int size) {}
