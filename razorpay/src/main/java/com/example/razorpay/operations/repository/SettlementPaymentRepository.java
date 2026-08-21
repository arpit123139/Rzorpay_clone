package com.example.razorpay.operations.repository;

import com.example.razorpay.operations.entity.SettlementPayment;
import com.example.razorpay.operations.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
