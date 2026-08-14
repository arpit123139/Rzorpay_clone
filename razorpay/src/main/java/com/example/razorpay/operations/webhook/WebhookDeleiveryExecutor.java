package com.example.razorpay.operations.webhook;

import com.example.razorpay.common.enums.WebhookEventStatus;
import com.example.razorpay.operations.entity.WebhookEvent;
import com.example.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDeleiveryExecutor {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue webhookRetryQueue;
    private final RestClient restClient;
    private final WebhookDlqRecorder webhookDlqRecorder;

    private static final List<Duration> BACKOFF = List.of(
            Duration.ofMinutes(1),  Duration.ofMinutes(5),  Duration.ofMinutes(30),
            Duration.ofHours(2), Duration.ofHours(8), Duration.ofHours(24)
    );

    @Value("${webhook.delivery.signature-header:X-Razorpay-Signature}")
    private String signatureHeader;

    private final int MAX_ATTEMPTS = 7;

    @Transactional
    public void deliver(UUID webhookEventId)
    {
        Optional<WebhookEvent> webhookEvent = webhookEventRepository.findById(webhookEventId);

        if(webhookEvent.isEmpty()){
            log.warn("No webhook Event found for this id ,{}",webhookEventId);
        }

        WebhookEvent event = webhookEvent.get();

        if(event.getStatus() == WebhookEventStatus.DEAD || event.getStatus() == WebhookEventStatus.DELIVERED ){
            log.warn("Cannot deleiver the event {} in status: {}",webhookEventId,event.getStatus());
        }

        event.setAttempts(event.getAttempts()+1);
        event.setLastAttemptAt(LocalDateTime.now());

        try {
           var response=  restClient.post()
                    .uri(event.getTargetUrl())
                    .header(signatureHeader, event.getSignature()) // signature contains the sign payload
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();

           int statusCode = response.getStatusCode().value();
           event.setLastResponseCode(statusCode);

           if(response.getStatusCode().is2xxSuccessful()){
               event.setStatus(WebhookEventStatus.DELIVERED);
               event.setDeliveredAt(LocalDateTime.now());
               webhookEventRepository.save(event);
               log.info("Successfully called the merchant for webhook Event with id {}",webhookEventId);
               return;
           }

           handleAttemptFailed(event,"HTTP"+statusCode);

        } catch (RestClientException e) {

            handleAttemptFailed(event,e.getMessage());
            log.error("Got Rest Client Exception ,{}",e.getMessage());
        }


    }

    private void handleAttemptFailed(WebhookEvent event, String error) {
        event.setLastResponseBody(error);

        if(event.getAttempts() >=MAX_ATTEMPTS){
            event.setStatus(WebhookEventStatus.DEAD);
            webhookDlqRecorder.recordAfterAttemptsExhausted(event,error);
            //dlq recording
            return;
        }

        Duration backoff = BACKOFF.get(event.getAttempts()-1);
        LocalDateTime nextReryAt = LocalDateTime.now().plus(backoff);
        event.setNextRetryAt(nextReryAt);
        event.setStatus(WebhookEventStatus.FAILED);
        webhookEventRepository.save(event);

        webhookRetryQueue.enqueue(event.getId(),nextReryAt);

        log.error("Handling attempt failed for webhook event {} with attempts {} , Next RETRY AT {}",event.getId() , event.getAttempts(),nextReryAt);
    }


}
