package com.demo.infrastructure.outbound.persistence;

import com.demo.domain.model.Order;
import com.demo.domain.port.out.OrderRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

// .await().indefinitely() is safe here — these methods are always called from @Blocking
// worker threads via OrderResource, never from the Vert.x event loop.
@ApplicationScoped
public class PanacheOrderRepository implements OrderRepository {

    @Inject
    OrderPanacheRepo repo;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        repo.persistAndFlush(entity).await().indefinitely();
        return entity.toDomain();
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return repo.findById(orderId)
                .map(e -> Optional.ofNullable(e).map(OrderEntity::toDomain))
                .await().indefinitely();
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return repo.find("customerId", customerId).list()
                .map(entities -> entities.stream().map(OrderEntity::toDomain).toList())
                .await().indefinitely();
    }

    @Override
    @Transactional
    public Order update(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        repo.persistAndFlush(entity).await().indefinitely();
        return entity.toDomain();
    }

    public Uni<Order> saveReactive(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        return repo.persistAndFlush(entity).map(e -> entity.toDomain());
    }

    public Uni<Optional<Order>> findByIdReactive(String orderId) {
        return repo.findById(orderId)
                .map(e -> Optional.ofNullable(e).map(OrderEntity::toDomain));
    }
}
