package com.example.distributed_razorpay.merchant_service.mapper;


import com.example.distributed_razorpay.merchant_service.dto.Response.WebhookConfigResponse;
import com.example.distributed_razorpay.merchant_service.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebhookConfigMapper {

    @Mapping(target = "webhookSecret",source = "rawSecret")
    WebhookConfigResponse toWebhookConfigResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret);
}
