package com.example.distributed_razorpay.payment_service.controller;


import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import com.example.distributed_razorpay.payment_service.dto.Request.PaymentInitRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;
import com.example.distributed_razorpay.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final PaymentService paymentService;
    private final MerchantContext merchantContext;


    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest request,@RequestHeader(value = "X-Idempotency-Key",required = false)String idempotencyKey){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(merchantContext.getMerchantId(),request,idempotencyKey));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }



}
