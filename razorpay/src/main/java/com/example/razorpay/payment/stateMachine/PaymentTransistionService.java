package com.example.razorpay.payment.stateMachine;

import com.example.razorpay.common.enums.PaymentActor;
import com.example.razorpay.common.enums.PaymentEvent;
import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.entity.PaymentTransitionLog;
import com.example.razorpay.payment.repository.PaymentRepository;
import com.example.razorpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransistionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final PaymentRepository paymentRepository;


    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){

        PaymentStatus next=paymentStateMachine.transition(payment.getStatus(),paymentEvent);




        PaymentTransitionLog log = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .event(paymentEvent)
                .actor(PaymentActor.SYSTEM)  //Either MerchantId or System
                .occurredAt(LocalDateTime.now())
                .build();

        payment.setStatus(next);

        paymentTransitionLogRepository.save(log);
        return next;
    }



}
