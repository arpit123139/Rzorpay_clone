package com.example.distributed_razorpay.vault_service.processor;


import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CardPaymentProcessor {
    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000069";

    @Bulkhead(name = "vault-card-processor",type = Bulkhead.Type.THREADPOOL)   // As I am using a Threadpool it will work only if we use a return a Completable Future
    public CompletableFuture<PaymentProcessorResponse> charge(PaymentProcessorRequest request) {
        String pan = request.pan();  // REAL PAN

        if (PAN_CARD_DECLINED.equals(pan)) {
            log.warn("Card declined");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by bank"));
        }

        if (PAN_CARD_EXPIRED.equals(pan)) {
            log.warn("Pan card has expired");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired"));
        }

        String processorRef = "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return  CompletableFuture.completedFuture(new PaymentProcessorResponse.Pending(processorRef));

    }
}
