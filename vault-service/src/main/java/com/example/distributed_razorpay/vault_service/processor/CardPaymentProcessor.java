package com.example.distributed_razorpay.vault_service.processor;


import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor {
    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000069";

    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        String pan = request.pan();  // REAL PAN

        if (PAN_CARD_DECLINED.equals(pan)) {
            log.warn("Card declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by bank");
        }

        if (PAN_CARD_EXPIRED.equals(pan)) {
            log.warn("Pan card has expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired");
        }

        String processorRef = "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);

    }
}
