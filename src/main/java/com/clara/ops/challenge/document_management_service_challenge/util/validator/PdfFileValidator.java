package com.clara.ops.challenge.document_management_service_challenge.util.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class PdfFileValidator implements ConstraintValidator<PdfFileOnly, MultipartFile> {
  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    return file != null && "application/pdf".equals(file.getContentType());
  }
}
