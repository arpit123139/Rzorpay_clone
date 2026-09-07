package com.example.distributed_razorpay.merchant_service.service;


import com.example.distributed_razorpay.merchant_service.dto.Request.CreateApiKeyRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.ApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.CreateApiKeyResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
     CreateApiKeyResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);

      List<ApiKeyResponse> list(UUID merchantId);

     void revoke(UUID merchantId, UUID keyId);


     CreateApiKeyResponse rotateKey(UUID merchantId, UUID keyId);
}
