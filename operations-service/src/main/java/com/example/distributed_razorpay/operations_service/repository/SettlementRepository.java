package com.example.distributed_razorpay.operations_service.repository;


import com.example.distributed_razorpay.common_lib.enums.SettlementStatus;
import com.example.distributed_razorpay.operations_service.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByStatus(SettlementStatus settlementStatus);


}
