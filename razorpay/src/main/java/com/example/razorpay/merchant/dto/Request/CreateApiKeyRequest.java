package com.example.razorpay.merchant.dto.Request;

import com.example.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
