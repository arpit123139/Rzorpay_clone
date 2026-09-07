package com.example.distributed_razorpay.merchant_service.repository;

import com.example.distributed_razorpay.merchant_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
     Optional<Customer> findByMerchant_IdAndEmail(UUID merchantId, String email);
}
