package com.example.distributed_razorpay.payment_service.gateway.dto;


import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;

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
