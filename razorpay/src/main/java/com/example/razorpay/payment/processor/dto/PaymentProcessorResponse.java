package com.example.razorpay.payment.processor.dto;

public sealed interface PaymentProcessorResponse permits PaymentProcessorResponse.Pending, PaymentProcessorResponse.Success, PaymentProcessorResponse.Failure , PaymentProcessorResponse.PendingNetBanking {
    //There can be multiple Record for multiple responses

    record  Pending(String processorReference) implements PaymentProcessorResponse{}

    record  PendingNetBanking(String processorReference,String redirectRef) implements PaymentProcessorResponse{}

    record Success(String processorReference,String bankReference) implements  PaymentProcessorResponse{}

    record Failure(String errorCode,String errorDescription) implements  PaymentProcessorResponse{}

}
