package com.satwik.splitora.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailOrPhoneValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailOrPhoneRequired {
    String message() default "Either email or phone number is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
