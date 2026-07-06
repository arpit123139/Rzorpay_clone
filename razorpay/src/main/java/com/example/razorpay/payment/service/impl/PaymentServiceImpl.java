package com.example.razorpay.payment.service.impl;

import com.example.razorpay.common.enums.OrderStatus;
import com.example.razorpay.common.enums.PaymentEvent;
import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.common.exceptions.BuisnessRuleViolationException;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.payment.dto.Request.PaymentInitRequest;
import com.example.razorpay.payment.dto.Response.PaymentResponse;
import com.example.razorpay.payment.entity.OrderRecord;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.gateway.PaymentGatewayRouter;
import com.example.razorpay.payment.gateway.dto.PaymentRequest;
import com.example.razorpay.payment.gateway.dto.PaymentResult;
import com.example.razorpay.payment.mapper.PaymentMapper;
import com.example.razorpay.payment.repository.OrderRepository;
import com.example.razorpay.payment.repository.PaymentRepository;
import com.example.razorpay.payment.service.PaymentService;
import com.example.razorpay.payment.stateMachine.PaymentStateMachine;
import com.example.razorpay.payment.stateMachine.PaymentTransistionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransistionService paymentTransitionService;


    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {

        OrderRecord orderRecord=orderRepository.findByIdAndMerchantId(request.orderId(),merchantId).orElseThrow(()->new ResourceNotFoundException("Order",request.orderId()));

        if(!(orderRecord.getOrderStatus()== OrderStatus.CREATED || orderRecord.getOrderStatus()== OrderStatus.ATTEMPTED))
            throw new BuisnessRuleViolationException("ORDER_NOT_PAYABLE","Order cannot accept payment in status :"+orderRecord.getOrderStatus());

        orderRecord.setOrderStatus(OrderStatus.ATTEMPTED);
        orderRecord.setAttempts(orderRecord.getAttempts()+1);

        Payment payment=Payment.builder()
                .order(orderRecord)
                .merchantId(merchantId)
                .amount(orderRecord.getAmount())
                .method(request.paymentMethod())
                .status(PaymentStatus.CREATED)
                .methodDetails(request.methodDetails())
                .build();

        paymentRepository.save(payment);

        //Process the Payment
        PaymentRequest paymentRequest=new PaymentRequest(payment.getId(),request.orderId(),merchantId,orderRecord.getAmount(),request.paymentMethod(),request.methodDetails());
        PaymentResult result= paymentGatewayRouter.initiate(paymentRequest);

        if(result instanceof PaymentResult.Pending pending){

            payment.setProcessorReference(pending.registrationRef());
        }else if(result instanceof PaymentResult.Failure failure){

            paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
        }

        //TODO: Send an kafka event
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if(paymentResult instanceof  PaymentResult.Success success) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment captured, paymentID: {}", paymentId);
        } else if(paymentResult instanceof  PaymentResult.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.warn("Payment capture failed, paymentID: {}", paymentId);
        }

        payment = paymentRepository.save(payment);

//        TODO: send an outbox (kafka event)

        return paymentMapper.toPaymentResponse(payment);
    }


}
