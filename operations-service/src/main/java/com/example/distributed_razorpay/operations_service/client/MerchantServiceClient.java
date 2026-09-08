package com.example.distributed_razorpay.operations_service.client;

import com.example.distributed_razorpay.common_lib.dto.SettlementBankDetails;
import com.example.distributed_razorpay.common_lib.dto.WebhookTarget;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "merchant-service",path = "/internal/merchants",url = "${MERCHANT_SERVICE_URI:}")
public interface MerchantServiceClient {

    @GetMapping("/{merchantId}/webhook-targets")
    List<WebhookTarget> getActiveConfigForEvent(@PathVariable UUID merchantId , @RequestParam String eventType);

    @GetMapping("/active-ids")
    List<UUID> listActiveMerchantIds();

    @GetMapping("/{merchantId}/settlement-bank--details")
    SettlementBankDetails getSettlementBankDetails(@PathVariable UUID merchantId);
}
