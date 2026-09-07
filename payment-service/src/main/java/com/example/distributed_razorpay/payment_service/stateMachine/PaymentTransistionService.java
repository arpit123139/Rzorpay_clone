package com.example.distributed_razorpay.payment_service.stateMachine;


import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import com.example.distributed_razorpay.common_lib.enums.PaymentActor;
import com.example.distributed_razorpay.common_lib.enums.PaymentEvent;
import com.example.distributed_razorpay.common_lib.enums.PaymentStatus;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import com.example.distributed_razorpay.payment_service.entity.PaymentTransitionLog;
import com.example.distributed_razorpay.payment_service.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransistionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final MerchantContext merchantContext;


    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){

        PaymentStatus next=paymentStateMachine.transition(payment.getStatus(),paymentEvent);

        PaymentActor actor = getPaymentActor();
        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .event(paymentEvent)
                .actor(actor)  //Either MerchantId or System
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(next);

        paymentTransitionLogRepository.save(log);
        return next;
    }

    private PaymentActor getPaymentActor(){
        try{
            String keyId = merchantContext.getKeyId();
            UUID merchantId = merchantContext.getMerchantId();
            if(keyId != null && !keyId.isBlank()){
                return PaymentActor.CUSTOMER;
            }
            else if(merchantId != null)
                return PaymentActor.MERCHANT;
        } catch (Exception e) {}

        return PaymentActor.SYSTEM;
    }


}
