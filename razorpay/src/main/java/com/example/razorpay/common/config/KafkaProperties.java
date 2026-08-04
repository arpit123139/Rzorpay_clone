package com.example.razorpay.common.config;

import com.example.razorpay.common.enums.EventAggregateType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Configuration
public class KafkaProperties {

    @Value("${app.kafka.topics.payment}")
    private String PAYMENT_TOPIC;

    @Value("${app.kafka.topics.order}")
    private String ORDER_TOPIC;

    @Value("${app.kafka.topics.refund}")
    private String REFUND_TOPIC;

    @Value("${app.kafka.topics.settlement}")
    private String SETTLEMENT_TOPIC;

    private Map<String, String> map;
    @PostConstruct
    public void Init(){
        map = Map.of(
                EventAggregateType.PAYMENT.name(), PAYMENT_TOPIC,
                EventAggregateType.ORDER.name(), ORDER_TOPIC,
                EventAggregateType.REFUND.name(), REFUND_TOPIC,
                EventAggregateType.SETTLEMENT.name(), SETTLEMENT_TOPIC
        );
    }



    public String topicFor(EventAggregateType aggregateType){
        String topic = map.get(aggregateType.name());

        if(topic==null)
            throw new IllegalStateException("No Kafka topic configured for aggregated type: "+aggregateType.name());

        return topic;
    }
}
