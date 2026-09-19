package com.demo.domain.port.in;

import com.demo.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface GetOrderUseCase {

    Optional<Order> findById(String orderId);

    List<Order> findByCustomer(String customerId);
}
