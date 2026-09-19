package com.demo.infrastructure.config;

import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;
import com.demo.domain.service.OrderDomainService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

// Una sola instancia de OrderDomainService compartida entre ambos use cases
@ApplicationScoped
public class DomainConfig {

    @Inject OrderRepository orderRepository;
    @Inject OrderEventPublisher orderEventPublisher;

    // Singleton compartido: CreateOrderUseCase y GetOrderUseCase apuntan a la misma instancia
    @Produces
    @ApplicationScoped
    public OrderDomainService orderDomainService() {
        return new OrderDomainService(orderRepository, orderEventPublisher);
    }

    @Produces
    @ApplicationScoped
    public CreateOrderUseCase createOrderUseCase(OrderDomainService service) {
        return service;
    }

    @Produces
    @ApplicationScoped
    public GetOrderUseCase getOrderUseCase(OrderDomainService service) {
        return service;
    }
}
