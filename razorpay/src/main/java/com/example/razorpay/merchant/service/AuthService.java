package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.dto.Request.LoginRequest;
import com.example.razorpay.merchant.dto.Request.MerchantSignUpRequest;
import com.example.razorpay.merchant.dto.Response.LoginResponse;
import com.example.razorpay.merchant.dto.Response.MerchantResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

public interface AuthService {

     MerchantResponse signUp(@Valid MerchantSignUpRequest request);

     LoginResponse login(@Valid LoginRequest request);
}
