package com.example.razorpay.payment.service;

import com.example.razorpay.payment.dto.Request.CreateOrderRequest;
import com.example.razorpay.payment.dto.Response.OrderResponse;
import com.example.razorpay.payment.dto.Response.PaymentResponse;
import jakarta.validation.constraints.Digits;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface OrderService {
     OrderResponse create(UUID merchantId, CreateOrderRequest request);

     OrderResponse getById(UUID merchantId, UUID orderId);

     OrderResponse cancel(UUID merchantId, UUID orderId);

     List<PaymentResponse> listPayment(UUID merchantId, UUID orderId);
}
