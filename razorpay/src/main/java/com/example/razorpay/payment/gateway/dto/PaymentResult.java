package com.example.razorpay.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Pending, PaymentResult.Failure ,PaymentResult.Success {

    record Pending(String registrationRef) implements  PaymentResult{}
    record Failure(String errorCode,String errorDescription) implements  PaymentResult{}

    //Only in case of refund we will get the success response otherwise all the payment request will be asynchronus paymentGateway will call payment Processor
    //it will get a reference and then payment Processor can do its job asynchronusly
    record Success(String bankReference) implements  PaymentResult{}
}
