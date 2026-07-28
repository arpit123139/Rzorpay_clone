package com.example.razorpay.merchant.mapper;

import com.example.razorpay.merchant.dto.Response.MerchantResponse;
import com.example.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface  MerchantMapper {

    @Mapping(source = "status",target = "merchantStatus")
    MerchantResponse toMerchantResponse(Merchant merchant);
}
