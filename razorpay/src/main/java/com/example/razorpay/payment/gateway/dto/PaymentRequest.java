package com.example.razorpay.payment.gateway.dto;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(

        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod paymentMethod,
        Map<String,Object> methodDetails
) {
}
