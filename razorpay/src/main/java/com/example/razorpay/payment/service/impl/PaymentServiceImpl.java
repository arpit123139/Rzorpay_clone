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
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.example.razorpay.payment.repository.OrderRepository;
import com.example.razorpay.payment.repository.PaymentRepository;
import com.example.razorpay.payment.service.PaymentService;
import com.example.razorpay.payment.stateMachine.PaymentStateMachine;
import com.example.razorpay.payment.stateMachine.PaymentTransistionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
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
                .idempotencyKey(UUID.randomUUID().toString())             //TODO: idempotency
                .status(PaymentStatus.CREATED)
                .methodDetails(request.methodDetails())
                .build();

        paymentRepository.save(payment);

        //Process the Payment
        PaymentRequest paymentRequest=new PaymentRequest(payment.getId(),
                request.orderId(),
                merchantId,
                orderRecord.getAmount(),
                request.paymentMethod(),
                request.methodDetails());

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult result= paymentGatewayRouter.initiate(paymentRequest);

        String redirectRef = null;
        switch (result){
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef()) ;
            case PaymentResult.PendingNetBanking pendingNetBanking -> {
                payment.setProcessorReference(pendingNetBanking.registrationRef());
                redirectRef = pendingNetBanking.redirectRef();
                 // We can redirect to the NetBanking website via redirectRef that is a URI recieve from the payment
                // processor as it has the prior knowledge of all the net banking websirte we sent this to the
                // frontend to redirect user to the website and mark the payment as Created

            }
            case PaymentResult.Failure failure ->{
                paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success-> {
                log.warn("Invalid state");
                return null;
            }
        }

        //TODO: Send an kafka event

        return new PaymentResponse(payment.getId(),
                merchantId,
                orderRecord.getId(),
                orderRecord.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getMethodDetails(),
                null,
                redirectRef,            // Only for the Net Banking
                payment.getErrorCode(),
                payment.getErrorDescription(),
                null
                );
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

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean resolve, String bankRef, String errorCode, String errorDescription) {

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()->new ResourceNotFoundException("Payement",paymentId.toString()));

        if(payment.getStatus()!=PaymentStatus.AUTHORIZING){
            log.warn("Payment is Not in Authorizing State");
            return;
        }

        OrderRecord orderRecord=payment.getOrder();

        if(resolve){
            paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());


            //Auto Capture
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(),paymentId);

            if(captureResult instanceof PaymentResult.Success){
                paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            }
            else if(captureResult instanceof PaymentResult.Failure failure){
                    paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAIL);
                    payment.setErrorCode(failure.errorCode());
                    payment.setErrorDescription(failure.errorDescription());
            }

            //TDOO : Send an Kafka Outbox event
        }
        else{
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
    }

}
