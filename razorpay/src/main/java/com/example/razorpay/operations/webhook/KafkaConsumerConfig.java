package com.example.razorpay.operations.webhook;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler webhookKafkaErrorHandler(
            //KafkaTemplate is Spring’s Kafka producer helper. In this case it is used to publish a failed record to a dead-letter topic.
            KafkaTemplate<Object, Object> kafkaTemplate) {

        //A “recoverer” runs after retries are exhausted.
        //By default, DeadLetterPublishingRecoverer publishes the failed record to:
        //original topic: payment-events
        //dead-letter topic: payment-events.DLT
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // 3 retry attempts, each 5 seconds apart.
        FixedBackOff backOff = new FixedBackOff(5_000L, 3L);

        //When the listener throws an exception, retry it according to backOff. If it still fails, call recoverer, which sends it to the DLT.
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, backOff);

        // Commit the original offset only after DLT recovery succeeds.
        errorHandler.setCommitRecovered(true);

        // Do not retry permanently invalid input.
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> kafkaListenerContainerFactory(
            ConsumerFactory<String, Map<String, Object>> consumerFactory,
            DefaultErrorHandler webhookKafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // ack.acknowledge() commits the processed record immediately.
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        factory.setCommonErrorHandler(webhookKafkaErrorHandler);

        return factory;
    }
}