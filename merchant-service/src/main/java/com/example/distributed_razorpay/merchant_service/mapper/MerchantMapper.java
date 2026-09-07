package com.example.distributed_razorpay.merchant_service.mapper;

import com.example.distributed_razorpay.merchant_service.dto.Response.MerchantResponse;
import com.example.distributed_razorpay.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface  MerchantMapper {

    @Mapping(source = "status",target = "merchantStatus")
    MerchantResponse toMerchantResponse(Merchant merchant);
}
