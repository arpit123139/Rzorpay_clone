package com.example.distributed_razorpay.payment_service.outbox;


import com.example.distributed_razorpay.common_lib.enums.EventAggregateType;
import com.example.distributed_razorpay.payment_service.entity.OutboxEvent;
import com.example.distributed_razorpay.payment_service.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class OutboxEventPublisher {

    private final OutBoxEventRepository outBoxEventRepository;

    public void publish(EventAggregateType eventAggregateType, UUID aggregateId , String eventType , Map<String,Object> payload){

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(eventAggregateType)
                .aggregatedId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outBoxEventRepository.save(outboxEvent);

    }
}
