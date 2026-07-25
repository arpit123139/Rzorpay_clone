package com.example.razorpay.common.ratelimit;

public interface RateLimiter {

    RateLimitingResult check(String key,int maxRequestAllowed,long windowSeconds);
}
