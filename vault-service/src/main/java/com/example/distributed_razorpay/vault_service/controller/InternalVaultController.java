package com.example.distributed_razorpay.vault_service.controller;

import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import com.example.distributed_razorpay.common_lib.dto.VaultChargeRequest;
import com.example.distributed_razorpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/vault")
public class InternalVaultController {


    private  final VaultService vaultService;

    @PostMapping("/charge")
    PaymentProcessorResponse charge(@RequestBody VaultChargeRequest request){
        vaultService.charge(request.paymentId(),request.token(),request.amount(),request.methodDetails());
    }
}
