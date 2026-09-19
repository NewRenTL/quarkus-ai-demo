package com.demo.infrastructure.outbound.persistence;

import com.demo.domain.model.Order;
import com.demo.domain.port.out.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SpringOrderRepository implements OrderRepository {

    private final JpaOrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        return jpaRepository.save(JpaOrderEntity.fromDomain(order)).toDomain();
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return jpaRepository.findById(orderId).map(JpaOrderEntity::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId)
                .stream().map(JpaOrderEntity::toDomain).toList();
    }

    @Override
    public Order update(Order order) {
        return jpaRepository.save(JpaOrderEntity.fromDomain(order)).toDomain();
    }
}
