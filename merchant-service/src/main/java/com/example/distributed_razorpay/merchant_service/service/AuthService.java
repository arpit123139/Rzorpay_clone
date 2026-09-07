package com.example.distributed_razorpay.merchant_service.service;


import com.example.distributed_razorpay.merchant_service.dto.Request.LoginRequest;
import com.example.distributed_razorpay.merchant_service.dto.Request.MerchantSignUpRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.LoginResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.MerchantResponse;
import jakarta.validation.Valid;

public interface AuthService {

     MerchantResponse signUp(@Valid MerchantSignUpRequest request);

     LoginResponse login(@Valid LoginRequest request);
}
