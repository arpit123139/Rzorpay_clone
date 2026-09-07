package com.example.distributed_razorpay.payment_service.controller;


import com.example.distributed_razorpay.common_lib.dto.PaymentSettlementView;
import com.example.distributed_razorpay.payment_service.api.PaymentLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalSettlementController {

   private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled-captured")
    List<PaymentSettlementView> findUnsettledCaptured(@RequestParam UUID merchantId){
         return paymentLookupService.findUnsettledCapturedPayments(merchantId);
    }

    @PostMapping("/mark-settled")
    void markSettled(@RequestBody List<UUID> paymentId){

    }
}
