package com.clara.ops.challenge.document_management_service_challenge.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PdfFileValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PdfFileOnly {
    String message() default "Only PDF files allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
