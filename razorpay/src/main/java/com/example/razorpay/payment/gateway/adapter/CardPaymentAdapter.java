package com.example.razorpay.payment.gateway.adapter;

import com.example.razorpay.payment.gateway.PaymentAdapter;
import com.example.razorpay.payment.gateway.dto.PaymentRequest;
import com.example.razorpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public class CardPaymentAdapter implements PaymentAdapter {
    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        return  null;
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
