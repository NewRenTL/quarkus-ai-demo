package com.demo.infrastructure.outbound.messaging;

import com.demo.domain.model.Order;
import com.demo.domain.port.out.OrderEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class KafkaOrderPublisher implements OrderEventPublisher {

    // Un Emitter por canal — cada topic tiene su propio canal configurado
    @Inject
    @Channel("orders-created")
    Emitter<String> ordersCreatedEmitter;

    @Inject
    @Channel("orders-approved")
    Emitter<String> ordersApprovedEmitter;

    @Inject
    @Channel("orders-rejected")
    Emitter<String> ordersRejectedEmitter;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void publishOrderCreated(Order order) {
        publish(ordersCreatedEmitter, order, "order-created");
    }

    @Override
    public void publishOrderApproved(Order order) {
        publish(ordersApprovedEmitter, order, "order-approved");
    }

    @Override
    public void publishOrderRejected(Order order) {
        publish(ordersRejectedEmitter, order, "order-rejected");
    }

    private void publish(Emitter<String> emitter, Order order, String eventType) {
        try {
            OrderEvent event = new OrderEvent(eventType, order.getId(),
                    order.getCustomerId(), order.getAmount(),
                    order.getCountry(), order.getStatus().name());

            String payload = objectMapper.writeValueAsString(event);

            Message<String> message = Message.of(payload)
                    .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                            .withKey(order.getId())
                            .build());

            emitter.send(message);
            log.debug("Published {} for order {}", eventType, order.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order event for order {}", order.getId(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}
