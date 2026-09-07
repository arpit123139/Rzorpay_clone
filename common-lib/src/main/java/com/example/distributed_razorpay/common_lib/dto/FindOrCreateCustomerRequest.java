package com.example.distributed_razorpay.common_lib.dto;

import java.util.UUID;

public record FindOrCreateCustomerRequest(

        UUID merchantId,
        String email,
        String name,
        String phone
) {
}
