package com.example.distributed_razorpay.merchant_service.controller;

import com.example.distributed_razorpay.common_lib.dto.SettlementBankDetails;
import com.example.distributed_razorpay.common_lib.dto.WebhookTarget;
import com.example.distributed_razorpay.merchant_service.api.MerchantLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/merchants")
public class InternalMerchantController {

    private final MerchantLookupService merchantLookupService;

    @GetMapping("/{merchantId}/webhook-targets")
    public List<WebhookTarget> getActiveConfigForEvent(@PathVariable UUID merchantId , @RequestParam String eventType){
        return merchantLookupService.getActiveConfigForEvent(merchantId,eventType);
    }

    @GetMapping("/active-ids")
    List<UUID> listActiveMerchantIds(){
        return merchantLookupService.listActiveMerchantIds();
    }

    @GetMapping("/{merchantId}/settlement-bank--details")
    SettlementBankDetails getSettlementBankDetails(@PathVariable UUID merchantId){
        return merchantLookupService.getSettlementBankDetail(merchantId);
    }
}
