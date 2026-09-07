package com.example.distributed_razorpay.merchant_service.controller;

import com.example.distributed_razorpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.example.distributed_razorpay.merchant_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/customers")
public class InternalCustomerController {

    private  final CustomerService customerService;

    @PostMapping("/find-or-create")
    UUID findOrCreate(@RequestBody FindOrCreateCustomerRequest request){
            return customerService.findorCreate(request.merchantId(),request.email(), request.name(),request.phone());
    }
}
