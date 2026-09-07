package com.example.distributed_razorpay.payment_service.controller;


import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import com.example.distributed_razorpay.payment_service.dto.Request.CreateOrderRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.OrderResponse;
import com.example.distributed_razorpay.payment_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(merchantContext.getMerchantId(),request));
    }
}
