package com.clara.ops.challenge.document_management_service_challenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DocumentUploadRequestDTO {
    @NotBlank
    private String user;
    @NotBlank
    private String documentName;
    @NotNull
    private List<String> tags;

    @JsonIgnore
    private MultipartFile file;

}
