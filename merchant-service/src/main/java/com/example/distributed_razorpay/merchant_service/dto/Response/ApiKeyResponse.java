package com.example.distributed_razorpay.merchant_service.dto.Response;


import com.example.distributed_razorpay.common_lib.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {
}
