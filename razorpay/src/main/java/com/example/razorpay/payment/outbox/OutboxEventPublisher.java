package com.example.razorpay.payment.outbox;

import com.example.razorpay.common.enums.EventAggregateType;
import com.example.razorpay.payment.entity.OutboxEvent;
import com.example.razorpay.payment.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
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
