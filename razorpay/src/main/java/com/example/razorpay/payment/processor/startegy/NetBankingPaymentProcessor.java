package com.example.razorpay.payment.processor.startegy;

import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails()!=null ? request.methodDetails().get("BANK").toString():null;

        if (bankCode.equals(BANK_CODE_FAIL)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED","Bank rejected the transaction registration");
        }

        String processorReference = "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        String redirectRef = "http://REDIRECT_BANK.com/"+processorReference;

        return new PaymentProcessorResponse.PendingNetBanking(processorReference,redirectRef);


    }
}
