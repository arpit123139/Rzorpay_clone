package com.example.razorpay.vault.service;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.example.razorpay.vault.dto.request.TokenizeRequest;
import com.example.razorpay.vault.dto.response.TokenizeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;


public interface VaultService {

    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody TokenizeRequest request, UUID merchantId);
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
