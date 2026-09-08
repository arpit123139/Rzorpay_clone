package com.example.distributed_razorpay.payment_service.saga;

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
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;
import com.example.distributed_razorpay.payment_service.mapper.PaymentMapper;
import com.example.distributed_razorpay.payment_service.outbox.OutboxEventPublisher;
import com.example.distributed_razorpay.payment_service.repository.OrderRepository;
import com.example.distributed_razorpay.payment_service.repository.PaymentRepository;
import com.example.distributed_razorpay.payment_service.stateMachine.PaymentTransistionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAuthorizationRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransistionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;


    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitRequest request,String idempotencyKey){

        OrderRecord orderRecord=orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(),merchantId).orElseThrow(()->new ResourceNotFoundException("Order",request.orderId()));

        if(!(orderRecord.getOrderStatus()== OrderStatus.CREATED || orderRecord.getOrderStatus()== OrderStatus.ATTEMPTED))
            throw new BuisnessRuleViolationException("ORDER_NOT_PAYABLE","Order cannot accept payment in status :"+orderRecord.getOrderStatus());

        orderRecord.setOrderStatus(OrderStatus.ATTEMPTED);
        orderRecord.setAttempts(orderRecord.getAttempts()+1);

        Payment payment=Payment.builder()
                .order(orderRecord)
                .merchantId(merchantId)
                .amount(orderRecord.getAmount())
                .method(request.paymentMethod())
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.CREATED)
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    @Transactional
    public PaymentResponse compensateAuthorizedFailure(UUID paymentId, String errorCode, String errorDescription,String redirectRef){

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()->new ResourceNotFoundException("Payment",paymentId.toString()));

        paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_AUTHORIZATION_COMPENSATED",
                Map.of("orderId",payment.getOrder().getId().toString(),
                        "paymentId",payment.getId().toString(),
                        "merchantId",payment.getMerchantId().toString(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency())
        );

        return new PaymentResponse(payment.getId(),
                payment.getMerchantId(),
                payment.getOrder().getId(),
                payment.getAmount(),
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


    @Transactional
    public PaymentResponse applyGatewayResult(UUID paymentId,PaymentResult result){

        log.info("Applying Gateway Result for PaymentId :{}",paymentId);

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()->new ResourceNotFoundException(
                "Payment",paymentId));

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

        payment =paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_CREATED",
                Map.of("orderId",payment.getOrder().getId().toString(),
                        "paymentId",payment.getId().toString(),
                        "merchantId",payment.getMerchantId().toString(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency())
        );

        log.info("Successfully Applied  Gateway Result for PaymentId :{}",paymentId);
        return new PaymentResponse(payment.getId(),
                payment.getMerchantId(),
                payment.getOrder().getId(),
                payment.getAmount(),
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

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> findExsistingAttempt(UUID merchantId,String idempotencyKey){
        return paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId,idempotencyKey).map(payment -> paymentMapper.toPaymentResponse(payment));
    }

}
