package com.example.razorpay.common.idempotency;

import com.example.razorpay.common.exceptions.IdempotencyConflictException;
import com.example.razorpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("PUT","POST","PATCH");
    private final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private final String SEPARATOR = "|";
    private final Duration COMPLETED_TTL = Duration.ofHours(24);

    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;


    private final HandlerExceptionResolver handlerExceptionResolver;




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        if(!GUARDED_METHODS.contains(request.getMethod())){
            chain.doFilter(request,response);
            return;
        }

        String rawKey = request.getHeader("X-IDEMPOTENCY-key");

        if(rawKey == null || rawKey.isBlank()){
            chain.doFilter(request,response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        // as we give the merchant the option to choose a rawKey so they may choose the same so we just added a merchantId to make it unique for every Merchant

        String key = merchantId!=null ? merchantId +":"+rawKey :rawKey;

        boolean claimed = idempotencyStore.setIfAbsent(key,IN_PROGRESS_TTL);

        if(!claimed)
        {
            //another thread has claimed it and as this thread was not able to set so returning false
            Optional<String> exsisting = idempotencyStore.get(key);

            if(exsisting.isPresent() && ! IdempotencyStore.IN_PROGRESS.equals(exsisting.get())){
                // it's not in progress and the key is present meaning the request was fullfilled
                replay(request,response,exsisting.get());
            }else {
                // It is in prgress by another Thread
                var ex = new IdempotencyConflictException("A request with idempotency key is in Progress");
                handlerExceptionResolver.resolveException(request,response,null,ex);

            }
            return;
        }

        // first time claim , allow this request to create Order
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);

        try {
            chain.doFilter(request, wrapper);
        } finally {
            int status = wrapper.getStatus();
            byte[] bodyBytes = wrapper.getContentAsByteArray();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            if (status < 400 && bodyBytes.length > 0) {
                // Success — store the completed response for future replays
                String stored = status + SEPARATOR + body;
                idempotencyStore.store(key, stored, COMPLETED_TTL);
                log.debug("IdempotencyFilter: stored response status={} key={}", status, key);
            } else {
                // Error or empty — delete placeholder so client can retry cleanly
                idempotencyStore.delete(key);
                log.debug("IdempotencyFilter: deleted placeholder after error status={} key={}", status, key);
            }
            // Always flush buffered body to the actual response.
            // If this is skipped the client receives an empty body.
            wrapper.copyBodyToResponse();
        }

    }

    private void replay(HttpServletRequest request, HttpServletResponse response, String stored) throws IOException {
        int separatorIndex = stored.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

        int status = Integer.parseInt(stored.substring(0, separatorIndex));
        String body = stored.substring(separatorIndex+1);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
