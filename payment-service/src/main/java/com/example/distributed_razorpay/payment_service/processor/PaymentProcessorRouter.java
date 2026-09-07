package com.example.distributed_razorpay.payment_service.processor;


import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
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
