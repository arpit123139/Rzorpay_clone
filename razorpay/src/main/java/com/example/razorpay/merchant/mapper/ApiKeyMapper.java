package com.example.razorpay.merchant.mapper;

import com.example.razorpay.merchant.dto.Response.ApiKeyResponse;
import com.example.razorpay.merchant.dto.Response.CreateApiKeyResponse;
import com.example.razorpay.merchant.entity.ApiKey;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    CreateApiKeyResponse toCreateApiKeyResponse(ApiKey apiKey);

    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);
}
