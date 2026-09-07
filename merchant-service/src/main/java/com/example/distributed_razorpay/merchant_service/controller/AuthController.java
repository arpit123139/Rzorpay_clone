package com.example.distributed_razorpay.merchant_service.controller;

import com.example.distributed_razorpay.merchant_service.dto.Request.LoginRequest;
import com.example.distributed_razorpay.merchant_service.dto.Request.MerchantSignUpRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.LoginResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.MerchantResponse;
import com.example.distributed_razorpay.merchant_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignUpRequest request){

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> signup(@RequestBody @Valid LoginRequest request){

        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}
