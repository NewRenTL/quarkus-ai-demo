package com.demo.domain.service;

import com.demo.domain.model.Order;
import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;

import java.util.List;
import java.util.Optional;

// Orquesta la lógica de negocio usando los puertos
// No importa si corre en Quarkus o Spring — misma clase, misma lógica
public class OrderDomainService implements CreateOrderUseCase, GetOrderUseCase {

    private final OrderRepository repository;
    private final OrderEventPublisher eventPublisher;

    public OrderDomainService(OrderRepository repository, OrderEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Order execute(CreateOrderCommand command) {
        Order order = Order.create(
                command.customerId(),
                command.customerEmail(),
                command.country(),
                command.items()
        ).validate(); // Reglas de negocio

        Order saved = repository.save(order);
        eventPublisher.publishOrderCreated(saved); // Notifica al mundo exterior

        return saved;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return repository.findById(orderId);
    }

    @Override
    public List<Order> findByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }
}
