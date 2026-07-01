package com.example.razorpay.merchant.mapper;

import com.example.razorpay.merchant.dto.Response.MerchantResponse;
import com.example.razorpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface  MerchantMapper {

    MerchantResponse toMerchantResponse(Merchant merchant);
}
