package com.example.razorpay.payment.controller;

import com.example.razorpay.payment.dto.Request.PaymentInitRequest;
import com.example.razorpay.payment.dto.Response.PaymentResponse;
import com.example.razorpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final PaymentService paymentService;
    UUID merchantId=UUID.fromString("b7f3289d-889c-4775-84e6-bd5bf1005f30");

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(merchantId,request));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.capture(merchantId, paymentId));
    }



}
