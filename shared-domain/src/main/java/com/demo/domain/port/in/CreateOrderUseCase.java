package com.demo.domain.port.in;

import com.demo.domain.model.Order;
import com.demo.domain.model.OrderItem;

import java.util.List;

// Puerto de entrada — define QUÉ puede hacer el sistema
// No sabe nada de HTTP, Kafka, ni base de datos
public interface CreateOrderUseCase {

    Order execute(CreateOrderCommand command);

    record CreateOrderCommand(
            String customerId,
            String customerEmail,
            String country,
            List<OrderItem> items
    ) {}
}
