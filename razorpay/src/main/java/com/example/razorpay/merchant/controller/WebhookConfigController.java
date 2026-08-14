package com.example.razorpay.merchant.controller;

import com.example.razorpay.merchant.dto.Request.UpdateWebhookConfigRequest;
import com.example.razorpay.merchant.dto.Response.WebhookConfigResponse;
import com.example.razorpay.merchant.security.MerchantContext;
import com.example.razorpay.merchant.service.WebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<WebhookConfigResponse> create(@Valid @RequestBody UpdateWebhookConfigRequest request){
        return ResponseEntity.ok(webhookConfigService.create(merchantContext.getMerchantId(),request));
    }

}
