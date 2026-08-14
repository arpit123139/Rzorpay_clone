package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.dto.Request.UpdateWebhookConfigRequest;
import com.example.razorpay.merchant.dto.Response.WebhookConfigResponse;

import java.util.UUID;

public interface WebhookConfigService {

    WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request);
}
