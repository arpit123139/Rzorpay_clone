package com.example.distributed_razorpay.merchant_service.controller;

import com.example.distributed_razorpay.common_lib.cache.ApiKeyCacheEntry;
import com.example.distributed_razorpay.merchant_service.entity.ApiKey;
import com.example.distributed_razorpay.merchant_service.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api-keys")
public class InternalApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @GetMapping("/{keyId}")
    ApiKeyCacheEntry findByKeyId(@PathVariable String keyId){

        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

        if(apiKey == null)
            return null;

        ApiKeyCacheEntry apiKeyCacheEntry= new ApiKeyCacheEntry(keyId,apiKey.getKeySecretHash(),
                apiKey.getPrevKeySecretHash(),
                apiKey.getGracePeriodExpiresAt(),apiKey.getMerchant().getId(),apiKey.getEnvironment(),
                apiKey.isEnabled());
        return apiKeyCacheEntry;
    }
}
