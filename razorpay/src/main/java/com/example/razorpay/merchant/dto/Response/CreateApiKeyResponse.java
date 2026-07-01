package com.example.razorpay.merchant.dto.Response;

import com.example.razorpay.common.enums.Environment;

import java.util.UUID;

public record CreateApiKeyResponse(

        UUID id, String keyId, String keySecret, Environment environment) {
}
