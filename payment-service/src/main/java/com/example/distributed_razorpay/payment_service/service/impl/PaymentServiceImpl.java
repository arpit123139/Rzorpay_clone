package com.example.distributed_razorpay.payment_service.service.impl;


import com.example.distributed_razorpay.common_lib.enums.EventAggregateType;
import com.example.distributed_razorpay.common_lib.enums.OrderStatus;
import com.example.distributed_razorpay.common_lib.enums.PaymentEvent;
import com.example.distributed_razorpay.common_lib.enums.PaymentStatus;
import com.example.distributed_razorpay.common_lib.exceptions.BuisnessRuleViolationException;
import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.payment_service.dto.Request.PaymentInitRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;
import com.example.distributed_razorpay.payment_service.entity.OrderRecord;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import com.example.distributed_razorpay.payment_service.gateway.PaymentGatewayRouter;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;
import com.example.distributed_razorpay.payment_service.mapper.PaymentMapper;
import com.example.distributed_razorpay.payment_service.outbox.OutboxEventPublisher;
import com.example.distributed_razorpay.payment_service.repository.OrderRepository;
import com.example.distributed_razorpay.payment_service.repository.PaymentRepository;
import com.example.distributed_razorpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.example.distributed_razorpay.payment_service.service.PaymentService;
import com.example.distributed_razorpay.payment_service.stateMachine.PaymentTransistionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
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
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Override
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {


        Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId,request);

        PaymentRequest paymentRequest=new PaymentRequest(payment.getId(),
                request.orderId(),
                merchantId,
                payment.getAmount(),
                request.paymentMethod(),
                request.methodDetails());

        PaymentResult result;

        try{
            result = paymentGatewayRouter.initiate(paymentRequest);
        } catch (Exception e) {
            return paymentAuthorizationRecorder.compensateAuthorizedFailure(payment.getId(),"PAYMENT_GATEWAY_ROUTER_UNREACHABLE",e.getMessage(),null);
        }

       return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(),result);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if(paymentResult instanceof  PaymentResult.Success success) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            OrderRecord orderRecord=payment.getOrder();
            orderRecord.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(orderRecord);
            log.info("Payment captured, paymentID: {}", paymentId);
        } else if(paymentResult instanceof  PaymentResult.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.warn("Payment capture failed, paymentID: {}", paymentId);
        }

        payment = paymentRepository.save(payment);


//        TODO: send an outbox (kafka event)

        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_STATUS_CHANGED",
                Map.of("orderId",payment.getOrder().getId().toString(),
                        "paymentId",payment.getId().toString(),
                        "merchantId",merchantId.toString(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency(),
                        "paymentMethod",payment.getMethod())
        );

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

        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_STATUS_CHANGED",
                Map.of("orderId",payment.getOrder().getId().toString(),
                        "paymentId",payment.getId().toString(),
                        "merchantId",payment.getMerchantId().toString(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency(),
                        "paymentMethod",payment.getMethod())
        );
    }

}
