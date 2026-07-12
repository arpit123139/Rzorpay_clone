package com.example.razorpay.payment.service;

import com.example.razorpay.payment.dto.Request.PaymentInitRequest;
import com.example.razorpay.payment.dto.Response.PaymentResponse;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId, PaymentInitRequest paymentInitRequest);

     PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean resolve, String bankRef, String errorCode, String errorDescription);
}
