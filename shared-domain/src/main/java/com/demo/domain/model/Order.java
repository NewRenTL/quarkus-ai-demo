package com.demo.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@With
public class Order {

    private final String id;
    private final String customerId;
    private final String customerEmail;
    private final String country;
    private final BigDecimal amount;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private OrderStatus status;
    private FraudRisk fraudRisk;

    public static Order create(String customerId, String customerEmail,
                                String country, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .customerEmail(customerEmail)
                .country(country)
                .amount(total)
                .items(items)
                .createdAt(Instant.now())
                .status(OrderStatus.PENDING)
                .fraudRisk(FraudRisk.UNKNOWN)
                .build();
    }

    // Reglas de negocio puras — sin framework, testeable de forma unitaria
    public Order validate() {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainException("Order amount must be positive");
        if (items == null || items.isEmpty())
            throw new DomainException("Order must have at least one item");
        if (customerId == null || customerId.isBlank())
            throw new DomainException("Customer ID is required");
        return this;
    }

    public Order markAsProcessing() {
        this.status = OrderStatus.PROCESSING;
        return this;
    }

    public Order markAsApproved() {
        this.status = OrderStatus.APPROVED;
        return this;
    }

    public Order markAsRejected() {
        this.status = OrderStatus.REJECTED;
        return this;
    }

    public Order withFraudRisk(FraudRisk risk) {
        this.fraudRisk = risk;
        return this;
    }

    public boolean isHighRisk() {
        return FraudRisk.HIGH == fraudRisk || FraudRisk.CRITICAL == fraudRisk;
    }
}
