package com.example.distributed_razorpay.vault_service.service;


import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.vault_service.dto.request.TokenizeRequest;
import com.example.distributed_razorpay.vault_service.dto.response.TokenizeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;


public interface VaultService {

    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody TokenizeRequest request, UUID merchantId);
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
