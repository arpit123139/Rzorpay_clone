package com.example.distributed_razorpay.payment_service.gateway;


import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod,PaymentAdapter>  paymentAdapter;

    public PaymentResult initiate(PaymentRequest request){

        PaymentAdapter adapter=paymentAdapter.get(request.paymentMethod());
        if(adapter==null){
            throw new IllegalArgumentException("No payment Adapter registered for method:"+request.paymentMethod());

        }
        return adapter.initiate(request);

    }

    public PaymentResult capture(PaymentMethod paymentMethod, UUID paymentId){

        PaymentAdapter adapter=paymentAdapter.get(paymentMethod);
        if(adapter==null){
            throw new IllegalArgumentException("No payment Adapter registered for method:"+paymentMethod);
        }

        return adapter.capture(paymentId);

    }
}
