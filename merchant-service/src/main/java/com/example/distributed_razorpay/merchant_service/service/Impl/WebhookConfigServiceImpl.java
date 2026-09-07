package com.example.distributed_razorpay.merchant_service.service.Impl;

import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import com.example.distributed_razorpay.merchant_service.dto.Request.UpdateWebhookConfigRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.WebhookConfigResponse;
import com.example.distributed_razorpay.merchant_service.entity.Merchant;
import com.example.distributed_razorpay.merchant_service.entity.MerchantWebhookConfig;
import com.example.distributed_razorpay.merchant_service.mapper.WebhookConfigMapper;
import com.example.distributed_razorpay.merchant_service.repository.MerchantRepository;
import com.example.distributed_razorpay.merchant_service.repository.WebhookConfigRepository;
import com.example.distributed_razorpay.merchant_service.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
