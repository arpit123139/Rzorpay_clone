package com.example.distributed_razorpay.merchant_service.service;



import com.example.distributed_razorpay.merchant_service.dto.Request.UpdateWebhookConfigRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.WebhookConfigResponse;

import java.util.UUID;

public interface WebhookConfigService {

    WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request);
}
