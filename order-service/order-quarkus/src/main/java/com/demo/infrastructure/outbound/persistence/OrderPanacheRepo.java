package com.demo.infrastructure.outbound.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderPanacheRepo implements PanacheRepositoryBase<OrderEntity, String> {
}
