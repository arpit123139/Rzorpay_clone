package com.example.distributed_razorpay.payment_service.gateway.adapter;


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
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {

        log.info("Initiate payment with Net Banking , paymentId is {}",request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest= PaymentProcessorRequest.nonCard(request.paymentId(), request.paymentMethod(),request.amount(),request.methodDetails());

            PaymentProcessorResponse paymentProcessorResponse= paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse){
                case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription()) ;
                case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.PendingNetBanking pendingNetBanking-> new PaymentResult.PendingNetBanking(pendingNetBanking.processorReference(),pendingNetBanking.redirectRef());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.error("");
            throw new RuntimeException(e);
        }

    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        //No call to Payment Processor for Capture as we already reguest the money from the bank (as the payment is
        // in Authorized state)
        return new PaymentResult.Success("UPI_REF");
    }
}
