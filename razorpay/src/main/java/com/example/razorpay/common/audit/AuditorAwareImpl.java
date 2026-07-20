package com.example.razorpay.common.audit;

import com.example.razorpay.merchant.security.MerchantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareImpl")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {

        try {
            if(merchantContext.getKeyId()!=null || !merchantContext.getKeyId().isBlank())
                return Optional.of(merchantContext.getKeyId());

            if(merchantContext.getMerchantId()!=null)
                return Optional.of("merchant_id: "+merchantContext.getMerchantId());
        } catch (Exception ignored) {

        }

        return Optional.of("SYSTEM");
    }
}
