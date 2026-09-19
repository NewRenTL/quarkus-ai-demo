package com.demo.infrastructure.config;

import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;
import com.demo.domain.service.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Mismo patrón que Quarkus — wiring del dominio puro con adaptadores Spring
// El OrderDomainService es IDENTICO al de Quarkus — es Java puro
@Configuration
public class DomainConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository repo,
                                                  OrderEventPublisher publisher) {
        return new OrderDomainService(repo, publisher);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository repo,
                                            OrderEventPublisher publisher) {
        return new OrderDomainService(repo, publisher);
    }
}
