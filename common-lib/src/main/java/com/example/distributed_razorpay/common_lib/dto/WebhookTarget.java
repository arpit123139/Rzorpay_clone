package com.example.distributed_razorpay.common_lib.dto;

import java.util.UUID;

public record WebhookTarget(UUID configId , String targetUrl,String webhookSecret) {
}
