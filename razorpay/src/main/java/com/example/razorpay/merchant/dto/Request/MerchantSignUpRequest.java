package com.example.razorpay.merchant.dto.Request;

import com.example.razorpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;


public record MerchantSignUpRequest(

        @NotNull(message = "Name is Required")
        @Size(max = 50,message = "Name Should not be more than 50 character")
        String name,

        @Email(message = "Email is Invalid")
        @NotNull(message = "Email is Required")
        String email,

        @NotNull(message = "Password is Required")
        @Size(max = 8,message = "Password should not be more that 8 character")
        String password,

        @Size(max = 50,message = "Buisness Name should not be more that 8 character")
        String buisnessName,

        BusinessType buisnessType

) {

}
