package com.example.distributed_razorpay.payment_service.processor.startegy;


import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import com.example.distributed_razorpay.payment_service.processor.PaymentProcessor;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails()!=null ? request.methodDetails().get("bank").toString():null;

        if (bankCode.equals(BANK_CODE_FAIL)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED","Bank rejected the transaction registration");
        }

        String processorReference = "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        String redirectRef = "http://REDIRECT_BANK.com/"+processorReference;

        return new PaymentProcessorResponse.PendingNetBanking(processorReference,redirectRef);


    }
}
