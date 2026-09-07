package com.example.distributed_razorpay.merchant_service.controller;

import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import com.example.distributed_razorpay.merchant_service.dto.Request.CreateApiKeyRequest;

import com.example.distributed_razorpay.merchant_service.dto.Response.ApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.CreateApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> create(@RequestBody @Valid CreateApiKeyRequest request){
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.create(merchantId,request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(){
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.status(HttpStatus.OK).body(apiKeyService.list(merchantId));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> delete( @PathVariable UUID keyId){
        UUID merchantId = merchantContext.getMerchantId();
        apiKeyService.revoke(merchantId,keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotate(@PathVariable UUID keyId){
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok().body(apiKeyService.rotateKey(merchantId,keyId));
    }
}
