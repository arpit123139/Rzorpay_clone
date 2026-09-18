package com.example.distributed_razorpay.api_gateway_service.security;

import com.example.distributed_razorpay.api_gateway_service.security.client.ApiKeyLookupClient;
import com.example.distributed_razorpay.common_lib.cache.ApiKeyCache;
import com.example.distributed_razorpay.common_lib.cache.ApiKeyCacheEntry;
import com.example.distributed_razorpay.common_lib.exceptions.RateLimitException;
import com.example.distributed_razorpay.common_lib.ratelimit.RateLimiter;
import com.example.distributed_razorpay.common_lib.ratelimit.RateLimitingResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class ApiKeyAuthHandler {

    private static final String BASIC_PREFIX = "Basic ";
    private static final String SECRET_VERIFY_PREFIX = "apikey:secret-verified:";
    private static final Duration SECRET_VERIFY_TTL = Duration.ofSeconds(30);

    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyLookupClient apiKeyLookupClient;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.rate-limit.use-case.api-key.request-per-minute:60}")
    private Integer requestPerMinute;


    public  Map<String,String> authenticate(String authHeader, HttpServletResponse response)  {

        String[] credentials = decode(authHeader);
        if(credentials == null)
            throw new GatewayAuthenticationException("Malformed API Key header");

        String keyId = credentials[0];
        String rawSecret = credentials[1];

        ApiKeyCacheEntry apiKey = apiKeyCache.get(keyId).orElseGet(()->loadAndCache(keyId));


        if ( apiKey == null || !apiKey.enabled() || !secretMatches(rawSecret, apiKey)) {
            throw new GatewayAuthenticationException("Invalid or missing API Key");
        }

        RateLimitingResult rateLimitingResult = rateLimiter.check("apikey:"+keyId,requestPerMinute,60);

        if(!rateLimitingResult.isAllowed()){
            log.warn("Too many Request keyId={}",keyId);
            throw new RateLimitException("Too Many Request",rateLimitingResult.retryAfterSeconds());
        }

        // In case of allowed request the limit did not exceeds
        response.setHeader("X-RateLimit-Limit",String.valueOf(requestPerMinute));
        response.setHeader("X-RateLimit-Remaining",String.valueOf(rateLimitingResult.remaining()));

        return Map.of(
                "X-Merchant-Id",apiKey.merchantId().toString(),
                "X-Environment",apiKey.environment().toString(),
                "X-Key-Id",apiKey.keyId()
        );

    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKeyCacheEntry apiKeyCacheEntry = apiKeyLookupClient.findByKeyId(keyId);

        if(apiKeyCacheEntry == null)
            return null;

        apiKeyCache.put(keyId,apiKeyCacheEntry);

        return apiKeyCacheEntry;

    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry entry) {

        String cacheKey = SECRET_VERIFY_PREFIX + entry.keyId() + ":" + entry.keySecretHash() + ":" + sha256(rawSecret);

        try {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey))) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Secret verification cache read failed, keyId: {}", entry.keyId());
        }

        boolean matches = BCRYPT.matches(rawSecret, entry.keySecretHash())
                || (entry.isInGracePeriod()
                && entry.previousKeySecretHash() != null
                && BCRYPT.matches(rawSecret, entry.previousKeySecretHash()));

        if (matches) {
            try {
                stringRedisTemplate.opsForValue().set(cacheKey, "true", SECRET_VERIFY_TTL);
            } catch (Exception e) {
                log.warn("Secret verification cache put failed, keyId: {}", entry.keyId());
            }
        }

        return matches;
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if (colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon+1)};
    }
}
