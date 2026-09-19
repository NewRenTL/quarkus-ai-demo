package com.demo.infrastructure.config;

import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;
import com.demo.domain.service.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// OrderDomainService implements both CreateOrderUseCase and GetOrderUseCase —
// a single bean satisfies both injection points.
@Configuration
public class DomainConfig {

    @Bean
    public OrderDomainService orderDomainService(OrderRepository repo,
                                                  OrderEventPublisher publisher) {
        return new OrderDomainService(repo, publisher);
    }
}
