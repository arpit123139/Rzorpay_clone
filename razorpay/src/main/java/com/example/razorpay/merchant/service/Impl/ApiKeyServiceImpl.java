package com.example.razorpay.merchant.service.Impl;

import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.merchant.dto.Request.CreateApiKeyRequest;
import com.example.razorpay.merchant.dto.Response.ApiKeyResponse;
import com.example.razorpay.merchant.dto.Response.CreateApiKeyResponse;
import com.example.razorpay.merchant.entity.ApiKey;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.mapper.ApiKeyMapper;
import com.example.razorpay.merchant.repository.ApiKeyRepository;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;

    @Override
    public CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant=merchantRepository.findById(merchantId)
                .orElseThrow( ()->new ResourceNotFoundException("merchant",merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret=RandomizerUtil.randomBase64(40);

        ApiKey apiKey=ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(rawSecret)    // TODO: ENCODE IT LATER USING BCRYPT PASSWORD ENCODER
                .environment(request.environment())
                .build();

        apiKey=apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(apiKey.getId(),keyId,rawSecret,request.environment()) ;
    }

    @Override
    public List<ApiKeyResponse> list(UUID merchantId) {
        return apiKeyRepository.findByMerchant_Id(merchantId).stream()
                .map(apiKey -> apiKeyMapper.toApiKeyResponse(apiKey)).toList();


    }

    @Override
    public void revoke(UUID merchantId, UUID keyId) {

        ApiKey key=apiKeyRepository.findById(keyId)
                .filter(apiKey -> apiKey.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("ApiKey",keyId));

        key.setEnabled(false);

        apiKeyRepository.save(key);
    }

    @Override
    public
    CreateApiKeyResponse rotateKey(UUID merchantId, UUID keyId) {
        ApiKey apiKey=apiKeyRepository.findById(keyId)
                .filter(apiKey1 -> apiKey1.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("ApiKey",keyId));

        if(!apiKey.isEnabled()){
            throw new RuntimeException("Cannot rotate a disabled key");
        }
        String newrawSecret=RandomizerUtil.randomBase64(40);

        apiKey.setPrevKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(newrawSecret);   // TODO: ENCODE IT LATER USING BCRYPT PASSWORD ENCODER
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKey=apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(apiKey.getId(),apiKey.getKeyId(),newrawSecret,apiKey.getEnvironment()) ;

    }
}
