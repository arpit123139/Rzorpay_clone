package com.example.distributed_razorpay.payment_service.processor;


import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest paymentProcessorRequest);
}
