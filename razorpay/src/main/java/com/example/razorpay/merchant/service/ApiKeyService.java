package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.dto.Request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.Response.ApiKeyResponse;
import com.example.razorpay.merchant.dto.Response.CreateApiKeyResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
     CreateApiKeyResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);

      List<ApiKeyResponse> list(UUID merchantId);

     void revoke(UUID merchantId, UUID keyId);


     CreateApiKeyResponse rotateKey(UUID merchantId, UUID keyId);
}
