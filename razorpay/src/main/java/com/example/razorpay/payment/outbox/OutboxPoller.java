package com.example.razorpay.payment.outbox;

import com.example.razorpay.common.config.KafkaProperties;
import com.example.razorpay.common.enums.OutboxStatus;
import com.example.razorpay.payment.entity.OutboxEvent;
import com.example.razorpay.payment.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutBoxEventRepository outBoxEventRepository;

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final Integer MAX_ATTEMPTS =  3;


    @Scheduled(fixedDelay = 5000)
    public void poll(){


        List<OutboxEvent> pendingEvent = outBoxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for(OutboxEvent event:pendingEvent){
            try {
                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());

                Map<String,Object> envelope = Map.of(
                        "eventType",event.getEventType(),
                        "aggregateType",event.getAggregateType(),
                        "aggregatedId",event.getAggregatedId(),
                        "data",event.getPayload()
                );


                kafkaTemplate.send(topic,key,envelope)
                        .get(5, TimeUnit.SECONDS);

                handleEventPublished(event);
            } catch (Exception e) {
                log.info("Outbox event failed , eventId: {} , attempts: {}",event.getId(),event.getAttempts());
                handleEventFailed(event,e.getMessage());
            }
        }


    }

    private void handleEventFailed(OutboxEvent event,String errorMessage) {
        event.setAttempts(event.getAttempts()+1);
        event.setLastError(
                errorMessage.length() < 1000 ? errorMessage :
                errorMessage.substring(0,1000));

        if(event.getAttempts()>MAX_ATTEMPTS)
            event.setStatus(OutboxStatus.FAILED);

        outBoxEventRepository.save(event);
    }

    private void handleEventPublished(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISH);
        event.setPublishedAt(LocalDateTime.now());
        outBoxEventRepository.save(event);
    }

    private String extractMerchantId(Map<String,Object> payload){

        Object value= payload.get("merchantId");
        return value!=null ? value.toString() : "unknown";
    }

}
