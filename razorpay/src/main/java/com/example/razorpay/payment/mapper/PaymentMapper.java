package com.example.razorpay.payment.mapper;

import com.example.razorpay.payment.dto.Response.PaymentResponse;
import com.example.razorpay.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id",target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);
}
