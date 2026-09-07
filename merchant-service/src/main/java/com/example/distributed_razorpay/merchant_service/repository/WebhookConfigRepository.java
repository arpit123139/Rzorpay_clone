package com.example.distributed_razorpay.merchant_service.repository;

import com.example.distributed_razorpay.merchant_service.entity.MerchantWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<MerchantWebhookConfig, UUID> {
    List<MerchantWebhookConfig> findByMerchantIdAndEnabledTrue(UUID merchantId);
}
