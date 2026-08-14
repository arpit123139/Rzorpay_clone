package com.example.razorpay.merchant.mapper;

import com.example.razorpay.merchant.dto.Response.WebhookConfigResponse;
import com.example.razorpay.merchant.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebhookConfigMapper {

    @Mapping(target = "webhookSecret",source = "rawSecret")
    WebhookConfigResponse  toWebhookConfigResponse(MerchantWebhookConfig merchantWebhookConfig,String rawSecret);
}
