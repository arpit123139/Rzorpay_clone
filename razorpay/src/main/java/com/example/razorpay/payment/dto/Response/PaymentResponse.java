package com.example.razorpay.payment.dto.Response;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.PaymentMethod;
import com.example.razorpay.common.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(

        UUID id,
        UUID merchantId,
        UUID orderId,
        Money amount,
        PaymentStatus status,
        PaymentMethod method,
        Map<String,Object> methodDetails,
        String bankReference,
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt
) {
}
