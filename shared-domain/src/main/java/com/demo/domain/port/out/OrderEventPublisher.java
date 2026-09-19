package com.demo.domain.port.out;

import com.demo.domain.model.Order;

// Puerto de salida para eventos — el dominio no sabe si es Kafka, RabbitMQ, etc.
public interface OrderEventPublisher {

    void publishOrderCreated(Order order);

    void publishOrderApproved(Order order);

    void publishOrderRejected(Order order);
}
