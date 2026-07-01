package com.example.razorpay.payment.dto.Response;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.OrderStatus;
import org.springframework.cglib.core.Local;

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
        LocalDateTime createdAt
){
}
