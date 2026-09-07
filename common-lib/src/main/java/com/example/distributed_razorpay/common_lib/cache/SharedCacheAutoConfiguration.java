package com.example.distributed_razorpay.common_lib.cache;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
public class SharedCacheAutoConfiguration {


    @Bean
    public ApiKeyCache apiKeyCache(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper){
        return new RedisApiKeyCache(stringRedisTemplate,objectMapper);
    }
}
