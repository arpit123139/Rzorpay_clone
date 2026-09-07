package com.example.distributed_razorpay.payment_service.repository;


import com.example.distributed_razorpay.common_lib.enums.OutboxStatus;
import com.example.distributed_razorpay.payment_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
