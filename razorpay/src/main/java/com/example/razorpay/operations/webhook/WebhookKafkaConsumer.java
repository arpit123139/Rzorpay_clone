package com.example.razorpay.operations.webhook;

import com.example.razorpay.common.dto.WebhookTarget;
import com.example.razorpay.common.enums.WebhookEventStatus;
import com.example.razorpay.common.utils.SignerUtil;
import com.example.razorpay.merchant.api.MerchantLookupService;
import com.example.razorpay.operations.entity.WebhookEvent;
import com.example.razorpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookKafkaConsumer {
    private final WebhookEventRepository webhookEventRepository;

    private final MerchantLookupService merchantLookupService;
    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDlqRecorder webhookDlqRecorder;
    @KafkaListener(topics = {
            "${app.kafka.topics.payment}",
            "${app.kafka.topics.order}",
            "${app.kafka.topics.refund}",
            "${app.kafka.topics.settlement}"
    })
    @Transactional
    public void onWebhookEvent(ConsumerRecord<String, Map<String,Object>> record,Acknowledgment ack){  //In the consumer record the value is the payload that we sent via producer and key is the key that we set(MerchantId) while producer sends the event to kafka

        try {
            Map<String,Object> envelope = record.value();
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");

            String eventType = (String) envelope.get("eventType");  // Single event (ORDER_CANCELLED , PAYMENT_CREATED)
            // COMMING FROM ORDER AND PAYMENT AND SETTLEMENT SERVICE

            Object merchantIdRaw = data.get("merchantId");
            if(merchantIdRaw == null)
            {
                log.info("No merchantId was found , skipping event: {}",eventType);
                ack.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString(merchantIdRaw.toString());

            //get me the list of targetURL for this event Type so we can send a webhook to the merchant to this targetURL
            List<WebhookTarget> targets = merchantLookupService.getActiveConfigForEvent(merchantId,eventType);
            if(targets.isEmpty()){
                ack.acknowledge();
                log.info("No Webhook target  was found , skipping event: {}",eventType);
                return;
            }

            Map<String,Object> signatureData = Map.of("event",eventType,"payload",data);
            String signatureJson = objectMapper.writeValueAsString(signatureData);

            for(WebhookTarget target: targets){
                String signature = signerUtil.sign(signatureJson,target.webhookSecret());

                WebhookEvent webhookEvent=WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEventRepository.save(webhookEvent);

                retryQueue.enqueue(webhookEvent.getId(),webhookEvent.getNextRetryAt());
                log.info("Created a webhook event with id {}",webhookEvent.getId());
            }

            ack.acknowledge();
        } catch (DataAccessException | CannotCreateTransactionException dbDown) {
            log.error("Webhook consumer failed due to DBDown to process the record , offset:{}",record.offset(),dbDown);
        }catch (Exception logicError){
            log.error("Webhook consumer failed due to Logic Error to process the record , offset:{}",record.offset(),
                    logicError);

            webhookDlqRecorder.recordConsumerFailed(record,logicError.getMessage());
            ack.acknowledge();
        }

    }
}
