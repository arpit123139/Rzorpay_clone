package com.example.razorpay.merchant.service.Impl;

import com.example.razorpay.common.dto.SettlementBankDetails;
import com.example.razorpay.common.dto.WebhookTarget;
import com.example.razorpay.common.enums.MerchantStatus;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.merchant.api.MerchantLookupService;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.mapper.WebhookConfigMapper;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantLookUpServiceImpl implements MerchantLookupService {

    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;

    @Override
    public List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchantIdAndEnabledTrue(merchantId).stream().filter(config-> config.isSubscribedTo(eventType))
                .map(config->
                        {
                            byte[] cipherBytes = Base64.getDecoder().decode(config.getWebhookSecret());
                            byte[] decryptedSecretBytes =
                                    bytesEncryptor.decrypt(cipherBytes);

                            return new WebhookTarget(config.getId(),config.getTargetUrl(),
                                    new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                        }
                ).toList();
    }

    @Override
    public List<UUID> listActiveMerchantIds() {

        List<Merchant> merchants = merchantRepository.findByStatus(MerchantStatus.ACTIVE);
        return merchants.stream().map(merchant -> merchant.getId()).toList();
    }

    @Override
    public SettlementBankDetails getSettlementBankDetail(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(()->new ResourceNotFoundException("Merchant",merchantId));

        return new SettlementBankDetails(merchant.getSettlementBankAccount(),merchant.getSettlementBankIfsc(),merchant.getSettlementBankAccountHolderName());
    }
}
