package com.example.razorpay.payment.controller;

import com.example.razorpay.payment.dto.Request.CreateOrderRequest;
import com.example.razorpay.payment.dto.Response.OrderResponse;
import com.example.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    UUID merchantId=UUID.fromString("b7f3289d-889c-4775-84e6-bd5bf1005f30");

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(merchantId,request));
    }
}
