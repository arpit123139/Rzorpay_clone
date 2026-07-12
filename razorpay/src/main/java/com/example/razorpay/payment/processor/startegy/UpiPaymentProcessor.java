package com.example.razorpay.payment.processor.startegy;

import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails()!=null ? request.methodDetails().get("vpa").toString():null;

        if (bankCode.equals(VPA_CODE_FAIL)){
            return new PaymentProcessorResponse.Failure("upi_rejected_REJECTED","Bank rejected the transaction registration");
        }

        String processorReference = "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        String bankRef = "BANK_REF"+processorReference;

        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
