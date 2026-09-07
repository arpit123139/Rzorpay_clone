package com.example.distributed_razorpay.merchant_service.service.Impl;


import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import com.example.distributed_razorpay.common_lib.cache.ApiKeyCache;
import com.example.distributed_razorpay.merchant_service.dto.Request.CreateApiKeyRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.ApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.CreateApiKeyResponse;
import com.example.distributed_razorpay.merchant_service.entity.ApiKey;
import com.example.distributed_razorpay.merchant_service.entity.Merchant;
import com.example.distributed_razorpay.merchant_service.mapper.ApiKeyMapper;
import com.example.distributed_razorpay.merchant_service.repository.ApiKeyRepository;
import com.example.distributed_razorpay.merchant_service.repository.MerchantRepository;
import com.example.distributed_razorpay.merchant_service.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // will avoid dirty checking
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant=merchantRepository.findById(merchantId)
                .orElseThrow( ()->new ResourceNotFoundException("merchant",merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret=RandomizerUtil.randomBase64(40);

        ApiKey apiKey=ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret))
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
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {

        ApiKey key=apiKeyRepository.findById(keyId)
                .filter(apiKey -> apiKey.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("ApiKey",keyId));

        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());
        apiKeyRepository.save(key);
    }

    @Override
    @Transactional
    public CreateApiKeyResponse rotateKey(UUID merchantId, UUID keyId) {
        ApiKey apiKey=apiKeyRepository.findById(keyId)
                .filter(apiKey1 -> apiKey1.getMerchant().getId().equals(merchantId))
                .orElseThrow(()->new ResourceNotFoundException("ApiKey",keyId));

        if(!apiKey.isEnabled()){
            throw new RuntimeException("Cannot rotate a disabled key");
        }
        String newrawSecret=RandomizerUtil.randomBase64(40);

        apiKey.setPrevKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(BCRYPT.encode(newrawSecret));   // TODO: ENCODE IT LATER USING BCRYPT PASSWORD ENCODER
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKey=apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());
        return new CreateApiKeyResponse(apiKey.getId(),apiKey.getKeyId(),newrawSecret,apiKey.getEnvironment()) ;

    }
}
