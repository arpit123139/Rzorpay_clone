package com.example.distributed_razorpay.payment_service.mapper;


import com.example.distributed_razorpay.payment_service.dto.Response.OrderResponse;
import com.example.distributed_razorpay.payment_service.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "merchantId",target = "merchant")
    OrderResponse toOrderResponse(OrderRecord orderRecord);
}
