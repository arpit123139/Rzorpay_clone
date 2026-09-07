package com.example.distributed_razorpay.payment_service.repository;


import com.example.distributed_razorpay.common_lib.enums.PaymentStatus;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrder_Id(UUID orderId);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Payment p where p.id=:paymentId and p.merchantId=:merchantId
            """)
    Optional<Payment> findByIdAndMerchantIdForUpdate(UUID paymentId, UUID merchantId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from Payyment p where p.merchantId=:merchantId and p.status=:paymentStatus and p.settledAt IS NULL
            """)
    List<Payment> findByMerchantIdAndStatusForUpdate(UUID merchantId, PaymentStatus paymentStatus);
}
