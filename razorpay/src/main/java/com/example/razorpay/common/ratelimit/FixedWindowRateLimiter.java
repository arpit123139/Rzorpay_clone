package com.example.razorpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method",havingValue = "fixed")
public class FixedWindowRateLimiter implements RateLimiter{

    private final StringRedisTemplate redisTemplate;


    @Override
    public RateLimitingResult check(String key, int maxRequestAllowed, long windowSeconds) {

        String redisKey= " ratelimit:fixed:"+key;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if(count==null) // Not able to connect with Redis , if the Redis connection is successfull but the key was not present then the previous operation will create a key assign a value 0 and increament and return 1
            return RateLimitingResult.allowed(maxRequestAllowed);

        if(count==1) // first time
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));

        if(count>maxRequestAllowed)
        {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);   // returns the remaining TTL duration
            int retryAfter = (ttl!=null && ttl>0) ? ttl.intValue() : (int)windowSeconds;
            return RateLimitingResult.denied(retryAfter);
        }

        return  RateLimitingResult.allowed((int) (maxRequestAllowed-count));

    }
}
