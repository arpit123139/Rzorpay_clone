package com.example.distributed_razorpay.payment_service.service;



import com.example.distributed_razorpay.payment_service.dto.Request.PaymentInitRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId, PaymentInitRequest paymentInitRequest);

     PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean resolve, String bankRef, String errorCode, String errorDescription);
}
