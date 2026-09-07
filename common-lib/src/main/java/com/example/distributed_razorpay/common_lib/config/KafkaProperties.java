package com.example.distributed_razorpay.common_lib.config;

import com.example.distributed_razorpay.common_lib.enums.EventAggregateType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties {

    private Map<String, String> topics = new HashMap<>();

    public String topicFor(EventAggregateType aggregateType) {
        String topic = topics.get(aggregateType.name().toLowerCase());
        if (topic == null) {
            throw new IllegalStateException("No Kafka topic is configured for aggregateType: "+aggregateType);
        }
        return topic;
    }

}
