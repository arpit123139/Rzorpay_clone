package com.example.distributed_razorpay.operations_service.repository;


import com.example.distributed_razorpay.operations_service.entity.SettlementPayment;
import com.example.distributed_razorpay.operations_service.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {

    @Query("""
            select id.paymentId from SettlementPayment where id.settlementId=:settlementId
            """
    )
    List<UUID> getAllSettlementPaymentBatchIds(@Param("settlementId") UUID settlementId);
}
