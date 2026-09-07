package com.example.distributed_razorpay.api_gateway_service.security;

public class GatewayAuthenticationException extends RuntimeException{

    public GatewayAuthenticationException(String msg){
        super(msg);
    }
}
