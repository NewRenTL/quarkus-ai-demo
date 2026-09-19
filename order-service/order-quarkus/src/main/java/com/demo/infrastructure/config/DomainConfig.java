package com.demo.infrastructure.config;

import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;
import com.demo.domain.service.OrderDomainService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

// OrderDomainService implements both CreateOrderUseCase and GetOrderUseCase,
// so a single @Produces bean satisfies all three injection points.
@ApplicationScoped
public class DomainConfig {

    @Inject OrderRepository orderRepository;
    @Inject OrderEventPublisher orderEventPublisher;

    @Produces
    @ApplicationScoped
    public OrderDomainService orderDomainService() {
        return new OrderDomainService(orderRepository, orderEventPublisher);
    }
}
