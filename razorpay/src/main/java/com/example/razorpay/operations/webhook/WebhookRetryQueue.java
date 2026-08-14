package com.example.razorpay.operations.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.model.jdbc.OptionalTableUpdateWithUpsertOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/*It uses a redis Database where it stores the value as a sorted set
value --> webhookEventId score -->nextRetryAt
                                                  Working
We will make a WebhookDelievery Scheduler that will poll for the event stored in the redis database whose score <= CurrentTime
and then try to make a http request to the merchant Server if the request is successfully acknowledge it will mark the webhookEvent status as Published in the WebhookEvent
Entity under operations
if not //Sad Scenario
then we will do a 7 times exponential retry after 1m,5m,30m,2h,8h,24h so to achieve this we will update the nextRetryAt accordingly and save it in the webhookEvent table under operationEntity

Now the work of taking the failed event from the DB and adding it to the redis will be done by another Scheduler that will check all the event with PENDING status and if they are
not present in redis it will put them in redis
*/
@Slf4j
public class WebhookRetryQueue {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.webhook.delivery.redis-key:webhook-retry}")
    private String key;

    public void enqueue(UUID webhookEventId, LocalDateTime retryAt){

        long time = getTime(retryAt);
        redisTemplate.opsForZSet().add(key,webhookEventId.toString(),time);
        log.info("Enqued a webhook Event with id ,{}",webhookEventId);
    }

    private static long getTime(LocalDateTime retryAt) {
        return retryAt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public Set<UUID> pollDue(int limit){
        long now = getTime(LocalDateTime.now());

        Set<ZSetOperations.TypedTuple<String>> due = redisTemplate.opsForZSet().rangeByScoreWithScores(key,0,now,0,limit);
        
        if(due ==null || due.isEmpty())
            return Set.of();

        due.forEach(tuple -> redisTemplate.opsForZSet().remove(key,tuple.getValue()));

        return due.stream().map(tuple -> UUID.fromString(tuple.getValue())).collect(Collectors.toSet());
    }

    public void enqueueIfAbsent(UUID id,  LocalDateTime retryAt){
        redisTemplate.opsForZSet().addIfAbsent(key,id.toString(),getTime(retryAt));
    }
}
