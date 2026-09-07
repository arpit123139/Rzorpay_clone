package com.example.distributed_razorpay.merchant_service.repository;


import com.example.distributed_razorpay.common_lib.enums.MerchantStatus;
import com.example.distributed_razorpay.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmail( String email);

    List<Merchant> findByStatus(MerchantStatus merchantStatus);
}
