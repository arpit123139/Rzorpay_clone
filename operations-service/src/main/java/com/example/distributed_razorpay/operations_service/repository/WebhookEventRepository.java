package com.example.distributed_razorpay.operations_service.repository;


import com.example.distributed_razorpay.common_lib.enums.WebhookEventStatus;
import com.example.distributed_razorpay.operations_service.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookEventRepository  extends JpaRepository<WebhookEvent, UUID> {
    List<WebhookEvent> findByStatusAndNextRetryAtBefore(WebhookEventStatus webhookEventStatus, LocalDateTime now);
}
