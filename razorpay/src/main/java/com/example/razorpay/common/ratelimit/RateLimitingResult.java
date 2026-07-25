package com.example.razorpay.common.ratelimit;

public record RateLimitingResult (boolean isAllowed,int remaining,int retryAfterSeconds){

    public static RateLimitingResult allowed(int remaining){
        return new RateLimitingResult(true,remaining,0);
    }

    public static RateLimitingResult denied(int retryAfterSeconds){
        return new RateLimitingResult(false,0,retryAfterSeconds);
    }

}
