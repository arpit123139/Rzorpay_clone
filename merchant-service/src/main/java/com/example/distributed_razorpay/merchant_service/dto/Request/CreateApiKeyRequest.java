package com.example.distributed_razorpay.merchant_service.dto.Request;


import com.example.distributed_razorpay.common_lib.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
