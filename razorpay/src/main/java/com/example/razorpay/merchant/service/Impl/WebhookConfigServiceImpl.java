package com.example.razorpay.merchant.service.Impl;

import com.example.razorpay.common.enums.MerchantStatus;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.merchant.api.MerchantLookupService;
import com.example.razorpay.merchant.dto.Request.UpdateWebhookConfigRequest;
import com.example.razorpay.merchant.dto.Response.WebhookConfigResponse;
import com.example.razorpay.common.dto.WebhookTarget;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.entity.MerchantWebhookConfig;
import com.example.razorpay.merchant.mapper.WebhookConfigMapper;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.repository.WebhookConfigRepository;
import com.example.razorpay.merchant.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final WebhookConfigMapper webhookConfigMapper;
    private final BytesEncryptor bytesEncryptor;

    @Override
    public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(()->new ResourceNotFoundException("Merchant",merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);

        String encryptedSecret = Base64.getEncoder().encodeToString(
                bytesEncryptor.encrypt(rawSecret.getBytes(StandardCharsets.UTF_8))
        );

        MerchantWebhookConfig config = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        config = webhookConfigRepository.save(config);

        return webhookConfigMapper.toWebhookConfigResponse(config,rawSecret);
    }

}
