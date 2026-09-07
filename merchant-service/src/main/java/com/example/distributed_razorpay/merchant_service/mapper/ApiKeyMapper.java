package com.example.distributed_razorpay.merchant_service.mapper;


import com.example.distributed_razorpay.merchant_service.dto.Response.ApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.CreateApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    CreateApiKeyResponse toCreateApiKeyResponse(ApiKey apiKey);

    ApiKeyResponse toApiKeyResponse(ApiKey apiKey);
}
