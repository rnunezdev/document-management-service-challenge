package com.clara.ops.challenge.document_management_service_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Data
public class DocumentUploadRequestDTO {
    @NotBlank
    private String user;
    @NotBlank
    private String documentName;
    @NotNull
    private List<String> tags;

    private MultipartFile file;

}
