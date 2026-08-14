package com.example.razorpay.merchant.api;

import com.example.razorpay.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantWebhookApi {

    List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType);
}
