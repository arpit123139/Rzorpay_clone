package com.example.distributed_razorpay.vault_service.service.Impl;


import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorRequest;
import com.example.distributed_razorpay.common_lib.dto.PaymentProcessorResponse;
import com.example.distributed_razorpay.common_lib.entity.Money;
import com.example.distributed_razorpay.common_lib.enums.CardBrand;
import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.common_lib.utils.RandomizerUtil;
import com.example.distributed_razorpay.vault_service.config.VaultEcrptionConfig;
import com.example.distributed_razorpay.vault_service.dto.request.TokenizeRequest;
import com.example.distributed_razorpay.vault_service.dto.response.TokenizeResponse;
import com.example.distributed_razorpay.vault_service.entity.CardToken;
import com.example.distributed_razorpay.vault_service.entity.VaultCard;
import com.example.distributed_razorpay.vault_service.processor.CardPaymentProcessor;
import com.example.distributed_razorpay.vault_service.repository.CardTokenRepository;
import com.example.distributed_razorpay.vault_service.repository.VaultCardRepository;
import com.example.distributed_razorpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final CardPaymentProcessor cardPaymentProcessor;

    private final BytesEncryptor dekEncryptor;

    @Override
    @Transactional
    public ResponseEntity<TokenizeResponse> tokenize(TokenizeRequest request, UUID merchantId) {

        String lastFour = request.pan().substring(request.pan().length()-4);
        String bin = request.pan().substring(0,6);

        CardBrand cardBrand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();

        byte[] encryptedPan = VaultEcrptionConfig.panEncryptor(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encrptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard =  VaultCard.builder()
                .brand(cardBrand)
                .expiryYear(request.expiryYear().toString())
                .expiryMonth(request.expiryMonth().toString())
                .bin(bin)
                .lastFour(lastFour)
                .encryptedDek(encrptedDek)
                .encryptedPan(encryptedPan)
                .cardHolderName(request.cardHolderName())
                .build();

        vaultCard = vaultCardRepository.save(vaultCard);

        String token = "tok_"+ RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(CardToken.builder()
                        .vaultCard(vaultCard)
                        .token(token)
                        .customer(request.customerId())
                        .merchant(merchantId)
                .build());

        return ResponseEntity.ok(new TokenizeResponse(token,lastFour,cardBrand, request.expiryMonth(),
                request.expiryYear()));

    }

   @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CardToken", token));

        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEcrptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest
                    .card(paymentId, pan, expiry, amount, methodDetails);

            PaymentProcessorResponse response = cardPaymentProcessor.charge(paymentProcessorRequest);

            log.info("Vault charge registered, token={}****", token.substring(0, 4));

            return response;
        } catch (Exception e) {
            log.warn("Vault charge failed, token={}****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if (panBytes != null) Arrays.fill(panBytes, (byte) 0);
        }
    }

    private CardBrand detectBrand(String pan) {
        if(pan.startsWith("4")) return CardBrand.VISA;
        if(pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if(pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;

        return CardBrand.RUPAY;
    }
}
