package com.example.distributed_razorpay.common_lib.ratelimit;

public interface RateLimiter {

    RateLimitingResult check(String key,int maxRequestAllowed,long windowSeconds);
}
