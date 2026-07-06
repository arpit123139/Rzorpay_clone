package com.example.razorpay.payment.processor;

import com.example.razorpay.common.enums.PaymentMethod;
import com.example.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod,PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor paymentProcessor=paymentProcessorMap.get(request.paymentMethod());

        if(paymentProcessor==null)
            throw  new IllegalArgumentException("No payment processor register for the method :  "+request.paymentMethod());

        return paymentProcessor.charge(request);
    }
}
