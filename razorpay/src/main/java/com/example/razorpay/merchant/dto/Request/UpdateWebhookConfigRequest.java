package com.example.razorpay.merchant.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookConfigRequest(

        @NotBlank(message = "Webhook URL is Required")
        @Size(max = 500)
        @Pattern(regexp = "^https?://.+" ,message = "Webhook URL must be a valid http(s) URL")
        String targetUrl,

        //Coomma separated fine graned event types (e.g "PAYMENT_STATUS_CHANGED","REFUND_CREATED")
        //Null/Blank/ALL means subscribes to every event
        @Size(max = 1000)
        String eventTypes
) {
}
