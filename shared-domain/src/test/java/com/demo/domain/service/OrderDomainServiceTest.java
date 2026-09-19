package com.demo.domain.service;

import com.demo.domain.model.*;
import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.out.OrderEventPublisher;
import com.demo.domain.port.out.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDomainServiceTest {

    @Mock OrderRepository repository;
    @Mock OrderEventPublisher eventPublisher;

    OrderDomainService service;

    @BeforeEach
    void setUp() {
        service = new OrderDomainService(repository, eventPublisher);
    }

    @Test
    void execute_validCommand_savesAndPublishesEvent() {
        var command = new CreateOrderUseCase.CreateOrderCommand(
                "cust-1", "cust@test.com", "PE",
                List.of(new OrderItem("p1", "Laptop", 1, BigDecimal.valueOf(1200)))
        );
        var savedOrder = Order.create(command.customerId(), command.customerEmail(),
                command.country(), command.items());
        when(repository.save(any())).thenReturn(savedOrder);

        Order result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1200));
        verify(repository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishOrderCreated(savedOrder);
    }

    @Test
    void execute_emptyItems_throwsDomainException() {
        var command = new CreateOrderUseCase.CreateOrderCommand(
                "cust-1", "cust@test.com", "PE", List.of()
        );
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("at least one item");

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishOrderCreated(any());
    }

    @Test
    void execute_blankCustomerId_throwsDomainException() {
        var command = new CreateOrderUseCase.CreateOrderCommand(
                "", "cust@test.com", "PE",
                List.of(new OrderItem("p1", "Mouse", 1, BigDecimal.TEN))
        );
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Customer ID");
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        var order = Order.create("c1", "c@t.com", "PE",
                List.of(new OrderItem("p1", "Item", 1, BigDecimal.TEN)));
        when(repository.findById("order-1")).thenReturn(Optional.of(order));

        Optional<Order> result = service.findById("order-1");

        assertThat(result).isPresent();
        verify(repository).findById("order-1");
    }

    @Test
    void findById_nonExistingOrder_returnsEmpty() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());
        assertThat(service.findById("ghost")).isEmpty();
    }

    @Test
    void orderIsHighRisk_whenFraudRiskCritical() {
        Order order = Order.create("c1", "c@t.com", "PE",
                List.of(new OrderItem("p1", "Item", 1, BigDecimal.TEN)));
        order.withFraudRisk(FraudRisk.CRITICAL);
        assertThat(order.isHighRisk()).isTrue();
    }

    @Test
    void orderIsNotHighRisk_whenFraudRiskLow() {
        Order order = Order.create("c1", "c@t.com", "PE",
                List.of(new OrderItem("p1", "Item", 1, BigDecimal.TEN)));
        order.withFraudRisk(FraudRisk.LOW);
        assertThat(order.isHighRisk()).isFalse();
    }
}
