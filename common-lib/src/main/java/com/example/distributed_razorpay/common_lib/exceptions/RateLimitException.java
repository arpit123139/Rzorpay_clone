package com.example.distributed_razorpay.common_lib.exceptions;


import lombok.Getter;

@Getter
public class  RateLimitException extends RuntimeException{

    private final int reteyAfterSecond;
    private final int remaining;

    public RateLimitException(String message,int retryAfterSecond){
        super(message);
        this.reteyAfterSecond=retryAfterSecond;
        this.remaining=0;
    }
}
