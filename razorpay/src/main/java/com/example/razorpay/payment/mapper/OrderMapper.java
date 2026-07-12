package com.example.razorpay.payment.mapper;

import com.example.razorpay.payment.dto.Response.OrderResponse;
import com.example.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "merchantId",target = "merchant")
    OrderResponse toOrderResponse(OrderRecord orderRecord);
}
