package com.example.distributed_razorpay.vault_service.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ExpiryYearValidator  implements ConstraintValidator<ExpiryYear,Integer> {

    @Override
    public boolean isValid(Integer inputYear, ConstraintValidatorContext constraintValidatorContext) {
        if(inputYear == null)
            return false ;

        int currentYear = Year.now().getValue();

        return inputYear>=currentYear;
    }
}
