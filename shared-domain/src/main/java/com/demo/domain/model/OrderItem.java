package com.demo.domain.model;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        String productName,
        int quantity,
        BigDecimal price
) {
    public OrderItem {
        if (quantity <= 0) throw new DomainException("Quantity must be positive");
        if (price.compareTo(BigDecimal.ZERO) < 0) throw new DomainException("Price cannot be negative");
    }
}
