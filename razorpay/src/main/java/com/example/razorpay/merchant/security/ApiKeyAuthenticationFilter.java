package com.example.razorpay.merchant.security;

import com.example.razorpay.common.exceptions.RateLimitException;
import com.example.razorpay.common.ratelimit.RateLimiter;
import com.example.razorpay.common.ratelimit.RateLimitingResult;
import com.example.razorpay.merchant.cache.ApiKeyCache;
import com.example.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.example.razorpay.merchant.entity.ApiKey;
import com.example.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.request-per-minute:60}")
    private Integer requestPerMinute;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Incoming request: {}", request.getRequestURI());

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }



            String[] credentials = decode(header);
            if (credentials == null) {
                throw new BadRequestException("Malformed API Key Header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKey = apiKeyCache.get(keyId).orElseGet(()->loadAndCache(keyId));


            if ( apiKey == null || !apiKey.enabled() || !secretMatches(rawSecret, apiKey)) {
                throw new BadRequestException("Invalid or missing API Key");
            }

            RateLimitingResult rateLimitingResult = rateLimiter.check("apikey:"+keyId,requestPerMinute,60);

            if(!rateLimitingResult.isAllowed()){
                log.warn("Too many Request keyId={}",keyId);
                throw new RateLimitException("Too Many Request",rateLimitingResult.retryAfterSeconds());
            }

            // In case of allowed request the limit did not exceeds
            response.setHeader("X-RateLimit-Limit",String.valueOf(requestPerMinute));
            response.setHeader("X-RateLimit-Remaining",String.valueOf(rateLimitingResult.remaining()));

            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("API_KEY_ROLE"))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKey.merchantId());
            merchantContext.setKeyId(apiKey.keyId());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

        if(apiKey == null)
            return null;

        ApiKeyCacheEntry apiKeyCacheEntry= new ApiKeyCacheEntry(keyId,apiKey.getKeySecretHash(),
                apiKey.getPrevKeySecretHash(),
                apiKey.getGracePeriodExpiresAt(),apiKey.getMerchant().getId(),apiKey.getEnvironment(),
                apiKey.isEnabled());

        apiKeyCache.put(keyId,apiKeyCacheEntry);

        return apiKeyCacheEntry;

    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey) {
        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())) {
            return true;
        }
        boolean isInGracePeriod = apiKey.gracePeriodExpiresAt() != null &&
                LocalDateTime.now().isBefore(apiKey.gracePeriodExpiresAt());
        return isInGracePeriod
                && apiKey.previousKeySecretHash() != null
                && BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash());
    }

    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(":");
        if (colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon+1)};
    }
}

















