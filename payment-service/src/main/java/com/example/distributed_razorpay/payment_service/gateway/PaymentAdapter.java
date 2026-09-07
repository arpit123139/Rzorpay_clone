package com.example.distributed_razorpay.payment_service.gateway;



import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    public PaymentResult initiate(PaymentRequest paymentRequest);

    PaymentResult capture(UUID paymentId);
}
