package com.demo.infrastructure.outbound.messaging;

import com.demo.domain.model.Order;
import com.demo.domain.port.out.OrderEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringKafkaOrderPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishOrderCreated(Order order) {
        publish("orders.created", order, "order-created");
    }

    @Override
    public void publishOrderApproved(Order order) {
        publish("orders.approved", order, "order-approved");
    }

    @Override
    public void publishOrderRejected(Order order) {
        publish("orders.rejected", order, "order-rejected");
    }

    private void publish(String topic, Order order, String eventType) {
        try {
            var event = new OrderEvent(eventType, order.getId(),
                    order.getCustomerId(), order.getAmount(),
                    order.getCountry(), order.getStatus().name());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, order.getId(), payload);
            log.debug("Published {} for order {}", eventType, order.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}
