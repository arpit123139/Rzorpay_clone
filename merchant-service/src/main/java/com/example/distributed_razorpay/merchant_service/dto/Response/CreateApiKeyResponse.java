package com.example.distributed_razorpay.merchant_service.dto.Response;


import com.example.distributed_razorpay.common_lib.enums.Environment;

import java.util.UUID;

public record CreateApiKeyResponse(

        UUID id, String keyId, String keySecret, Environment environment) {
}
