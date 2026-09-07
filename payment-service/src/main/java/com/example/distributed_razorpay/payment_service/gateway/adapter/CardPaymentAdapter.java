package com.example.distributed_razorpay.payment_service.gateway.adapter;


import com.example.distributed_razorpay.common_lib.dto.VaultChargeRequest;
import com.example.distributed_razorpay.common_lib.exceptions.BuisnessRuleViolationException;
import com.example.distributed_razorpay.payment_service.client.VaultServiceClient;
import com.example.distributed_razorpay.payment_service.gateway.PaymentAdapter;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentRequest;
import com.example.distributed_razorpay.payment_service.gateway.dto.PaymentResult;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultServiceClient.charge(
                new VaultChargeRequest(
                        request.paymentId(),
                        token,
                        request.amount(),
                        request.methodDetails()
                )
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
