package com.example.distributed_razorpay.payment_service.dto.Response;


import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import com.example.distributed_razorpay.common_lib.enums.PaymentStatus;
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
        String redirectRef, // Only in case of Net Banking
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt
) {
}
