package com.demo.domain.port.out;

import com.demo.domain.model.Order;

import java.util.List;
import java.util.Optional;

// Puerto de salida — define lo que el dominio necesita del mundo exterior
// La implementación real (PostgreSQL, MongoDB, etc.) va en los adaptadores
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByCustomerId(String customerId);

    Order update(Order order);
}
