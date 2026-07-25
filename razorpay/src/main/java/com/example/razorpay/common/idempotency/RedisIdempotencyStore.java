package com.example.razorpay.common.idempotency;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyStore implements  IdempotencyStore{

    private final StringRedisTemplate redis;
    private final String PREFIX = "idempotency:";

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {

        try{
            Boolean set = redis.opsForValue().setIfAbsent(PREFIX+key,IN_PROGRESS,ttl);
            return Boolean.TRUE.equals(set); // return false in case of when the key was present or it returns null

        } catch (DataAccessException e) {
            log.warn("Idempotency store unavailable, failing open for key={}",key,e);
            return true;
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redis.opsForValue().set(PREFIX+key,value,ttl);
        }catch (DataAccessException e){
            log.warn("Failed to persist , failing open for key={}",key,e);
        }

    }

    @Override
    public Optional<String> get(String key) {
        try{
            return Optional.ofNullable(redis.opsForValue().get(PREFIX+key));
        }catch (DataAccessException e){
            log.warn("Failed to persist , failing open for key={}",key,e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {

        try{
            redis.delete(PREFIX+key);
        }catch (DataAccessException e){
            log.warn("Failed to delete idempotency key={}",key,e);

        }
    }
}
