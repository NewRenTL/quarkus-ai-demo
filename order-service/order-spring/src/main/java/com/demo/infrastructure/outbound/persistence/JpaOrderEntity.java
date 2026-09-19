package com.demo.infrastructure.outbound.persistence;

import com.demo.domain.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class JpaOrderEntity {

    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "country")
    private String country;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private FraudRisk fraudRisk;

    @Column(name = "created_at")
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<JpaOrderItemEmbeddable> items;

    public static JpaOrderEntity fromDomain(Order order) {
        return JpaOrderEntity.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .country(order.getCountry())
                .amount(order.getAmount())
                .status(order.getStatus())
                .fraudRisk(order.getFraudRisk())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(i -> new JpaOrderItemEmbeddable(
                                i.productId(), i.productName(), i.quantity(), i.price()))
                        .toList())
                .build();
    }

    public Order toDomain() {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .customerEmail(customerEmail)
                .country(country)
                .amount(amount)
                .status(status)
                .fraudRisk(fraudRisk)
                .createdAt(createdAt)
                .items(items.stream()
                        .map(i -> new OrderItem(
                                i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                        .toList())
                .build();
    }
}
