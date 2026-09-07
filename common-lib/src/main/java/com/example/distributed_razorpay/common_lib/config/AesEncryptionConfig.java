package com.example.distributed_razorpay.common_lib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
@Configuration
public class AesEncryptionConfig {


    @Bean
    public BytesEncryptor masterKeyEncryptor(String masterKey){
        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
        SecretKeySpec decKey = new SecretKeySpec(masterKeyBytes,"AES");
        return new AesBytesEncryptor(decKey, KeyGenerators.secureRandom(12),AesBytesEncryptor.CipherAlgorithm.GCM);
    }

}
