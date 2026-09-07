package com.example.distributed_razorpay.payment_service.mapper;


import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id",target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);
}
