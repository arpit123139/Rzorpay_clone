package com.example.distributed_razorpay.payment_service.dto.Response;



import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.common_lib.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse (

        UUID id,
        UUID merchant,
        String receipt,
        Money amount,
        OrderStatus orderStatus,
        Integer attempts,
        Map<String,Object> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        UUID customerId
){
}
