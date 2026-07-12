package com.example.razorpay.payment.gateway.adapter;

import com.example.razorpay.common.exceptions.BuisnessRuleViolationException;
import com.example.razorpay.payment.gateway.PaymentAdapter;
import com.example.razorpay.payment.gateway.dto.PaymentRequest;
import com.example.razorpay.payment.gateway.dto.PaymentResult;
import com.example.razorpay.payment.processor.dto.PaymentProcessorResponse;
import com.example.razorpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultService.charge(
                request.paymentId(), token, request.amount(), request.methodDetails()
        );

        return switch (response) {
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
            case PaymentProcessorResponse.PendingNetBanking pendingNetBanking-> throw new BuisnessRuleViolationException("WRONG_RESPONSE_TYPE","Wrong Response from PaymentProcessor for Card Payment") ;
        };

    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
