package com.example.razorpay.operations.repository;

import com.example.razorpay.common.enums.SettlementStatus;
import com.example.razorpay.operations.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByStatus(SettlementStatus settlementStatus);
}
