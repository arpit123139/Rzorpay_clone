package com.example.distributed_razorpay.payment_service.service;



import com.example.distributed_razorpay.payment_service.dto.Request.CreateOrderRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.OrderResponse;
import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
     OrderResponse create(UUID merchantId, CreateOrderRequest request);

     OrderResponse getById(UUID merchantId, UUID orderId);

     OrderResponse cancel(UUID merchantId, UUID orderId);

     List<PaymentResponse> listPayment(UUID merchantId, UUID orderId);
}
