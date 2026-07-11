package com.example.razorpay.vault.Validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {ExpiryYearValidator.class})
public @interface ExpiryYear {

    String message() default "Expiry year cannot be past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
