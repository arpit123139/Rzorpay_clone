package com.example.razorpay.vault.dto.request;

import com.example.razorpay.vault.Validation.ExpiryYear;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;


public record TokenizeRequest (

        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid Card number")
        @Pattern(regexp = "^[0-9]{13,19}$" , message = "PAN length is invalid")
        String pan,

        @NotBlank(message = "CVV is Required")
        @Pattern(regexp = "^[0-9]{3,4}$" ,message = "CVV length is invalid")
        String cvv,

        @NotNull(message = "Expiry Month is required")
        @Min(value = 1,message = "Expiry must be between 1 to 12")
        @Max(value = 12,message = "Expiry must be between 1 to 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry Year is required")
        @ExpiryYear
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3,message = "Card Holder Name should have at least 3 characters")
        String cardHolderName
){

}
