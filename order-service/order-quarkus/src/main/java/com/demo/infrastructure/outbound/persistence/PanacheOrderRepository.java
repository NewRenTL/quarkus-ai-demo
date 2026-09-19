package com.demo.infrastructure.outbound.persistence;

import com.demo.domain.model.Order;
import com.demo.domain.port.out.OrderRepository;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

// .await().indefinitely() is safe here — these methods are always called from @Blocking
// worker threads, never from the Vert.x event loop.
@ApplicationScoped
public class PanacheOrderRepository
        implements OrderRepository, PanacheRepositoryBase<OrderEntity, String> {

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        persistAndFlush(entity).await().indefinitely();
        return entity.toDomain();
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return findByIdOptional(orderId)
                .map(opt -> opt.map(OrderEntity::toDomain))
                .await().indefinitely();
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return find("customerId", customerId).list()
                .map(entities -> entities.stream().map(OrderEntity::toDomain).toList())
                .await().indefinitely();
    }

    @Override
    @Transactional
    public Order update(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        persistAndFlush(entity).await().indefinitely();
        return entity.toDomain();
    }

    public Uni<Order> saveReactive(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        return persistAndFlush(entity).map(OrderEntity::toDomain);
    }

    public Uni<Optional<Order>> findByIdReactive(String orderId) {
        return findByIdOptional(orderId)
                .map(opt -> opt.map(OrderEntity::toDomain));
    }
}
