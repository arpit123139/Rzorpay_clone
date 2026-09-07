package com.example.distributed_razorpay.merchant_service.controller;


import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import com.example.distributed_razorpay.merchant_service.dto.Request.UpdateWebhookConfigRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.WebhookConfigResponse;
import com.example.distributed_razorpay.merchant_service.service.WebhookConfigService;
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
