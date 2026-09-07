package com.example.distributed_razorpay.payment_service.dto.Request;

import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(

        @NotNull(message="Order Id is required")
        UUID orderId,

        @NotNull(message = "Payment Method is Required")
        PaymentMethod paymentMethod,

        Map<String,Object> methodDetails
) {
}
