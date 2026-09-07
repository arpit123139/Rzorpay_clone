package com.example.distributed_razorpay.merchant_service.repository;

import com.example.distributed_razorpay.merchant_service.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchant_id);

    Optional<ApiKey> findByKeyId(String keyId);
}
