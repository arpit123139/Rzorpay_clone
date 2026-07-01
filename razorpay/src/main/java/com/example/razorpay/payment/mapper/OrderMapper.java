package com.example.razorpay.payment.mapper;

import com.example.razorpay.payment.dto.Response.OrderResponse;
import com.example.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(OrderRecord orderRecord);
}
