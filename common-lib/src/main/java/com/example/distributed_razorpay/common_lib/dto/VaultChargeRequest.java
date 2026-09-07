package com.example.distributed_razorpay.common_lib.dto;

import com.example.distributed_razorpay.common_lib.entity.Money;

import java.util.Map;
import java.util.UUID;

public record VaultChargeRequest(

        UUID paymentId,
        String token,
        Money amount,
        Map<String,Object> methodDetails
) {
}
