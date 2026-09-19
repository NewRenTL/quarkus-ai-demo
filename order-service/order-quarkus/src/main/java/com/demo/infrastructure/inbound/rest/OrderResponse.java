package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderResponse {

    private String id;
    private String customerId;
    private String customerEmail;
    private String country;
    private BigDecimal amount;
    private String status;
    private String fraudRisk;
    private Instant createdAt;
    private int itemCount;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .country(order.getCountry())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .fraudRisk(order.getFraudRisk().name())
                .createdAt(order.getCreatedAt())
                .itemCount(order.getItems().size())
                .build();
    }
}
