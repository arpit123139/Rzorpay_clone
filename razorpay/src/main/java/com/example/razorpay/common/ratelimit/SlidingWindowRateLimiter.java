package com.example.razorpay.common.ratelimit;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method",havingValue = "sliding")
public class SlidingWindowRateLimiter implements RateLimiter{

    private final StringRedisTemplate redis;

    @Override
    public  RateLimitingResult  check(String key, int maxRequestAllowed, long windowSeconds) {

        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - windowSeconds*1000;

        // We need to count the umber of request bwt  floorMs---nowMs

        String redisKey = "ratelimit:sliding:"+key;

        var zset = redis.opsForZSet();
        // remove all the entry which are less than or equal to floorMs
        // window second = 4sec;
        // current time t=8;
        // we should count the number of request from t=5 --- t=8 (4 sec window  (request at t=4 is considerd old and not counted)) thus removing all the entry which are less than floorMs or less than 4 sec
        zset.removeRangeByScore(redisKey,Double.NEGATIVE_INFINITY,floorMs);

        Long count = zset.zCard(redisKey);
        long current = (count!=null ? count:0);

        if(current>=maxRequestAllowed)
        {
            //topkey(oldest key in terms of time) score in set - currentTime
            var oldest = zset.rangeWithScores(redisKey,0,0); // getting first index in sorted Set
            int retryAfter = 1;

            if(oldest!=null && !oldest.isEmpty()){
                Double oldestScore = oldest.iterator().next().getScore();
                if(oldestScore !=null){
                    retryAfter = (int)Math.ceil((oldestScore - floorMs)/1000);
                }
            }
            return RateLimitingResult.denied(retryAfter);

        }

        zset.add(redisKey, UUID.randomUUID().toString(),nowMs);
        redis.expire(redisKey, Duration.ofSeconds(windowSeconds+1));
        return RateLimitingResult.allowed((int) (maxRequestAllowed-current-1 ));
    }
}
