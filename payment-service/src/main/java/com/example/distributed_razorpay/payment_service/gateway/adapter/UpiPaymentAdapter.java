package com.example.distributed_razorpay.payment_service.gateway.adapter;


import com.example.distributed_razorpay.common_lib.exceptions.BuisnessRuleViolationException;
import com.example.distributed_razorpay.payment_service.gateway.PaymentAdapter;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;
import com.example.distributed_razorpay.payment_service.processor.PaymentProcessorRouter;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {

        log.info("Initiate payment with UPI  , paymentId is {}",request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest=PaymentProcessorRequest.nonCard(request.paymentId(), request.paymentMethod(),request.amount(),request.methodDetails());

            PaymentProcessorResponse paymentProcessorResponse= paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse){
                case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription()) ;
                case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.PendingNetBanking pendingNetBanking-> throw new BuisnessRuleViolationException("WRONG_RESPONSE_TYPE","Wrong Response from PaymentProcessor for UPI") ;
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.error(" UPI Failed paymentId {}",request.paymentId());
            return new PaymentResult.Failure("UPi_PAYMENT_FAILED","Payment Fialed "+request.paymentId());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NBK_REF");
    }
}

