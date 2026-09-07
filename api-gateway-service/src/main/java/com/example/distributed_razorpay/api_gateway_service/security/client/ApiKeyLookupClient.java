package com.example.distributed_razorpay.api_gateway_service.security.client;

import com.example.distributed_razorpay.common_lib.cache.ApiKeyCacheEntry;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "merchant-service",path = "/internal/api-keys")
public interface ApiKeyLookupClient {

    @GetMapping("/{keyId}")
    ApiKeyCacheEntry findByKeyId(@PathVariable String keyId);
}
