package com.example.razorpay.merchant.controller;

import com.example.razorpay.merchant.dto.Request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.Response.ApiKeyResponse;
import com.example.razorpay.merchant.dto.Response.CreateApiKeyResponse;
import com.example.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> create(@PathVariable UUID merchantId, @RequestBody @Valid CreateApiKeyRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.create(merchantId,request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list(@PathVariable UUID merchantId){
        return ResponseEntity.status(HttpStatus.OK).body(apiKeyService.list(merchantId));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> delete(@PathVariable UUID merchantId ,@PathVariable UUID keyId){
        apiKeyService.revoke(merchantId,keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotate(@PathVariable UUID merchantId ,@PathVariable UUID keyId){

        return ResponseEntity.ok().body(apiKeyService.rotateKey(merchantId,keyId));
    }
}
