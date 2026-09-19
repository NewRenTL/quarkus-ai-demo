package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.*;
import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class OrderResourceTest {

    @InjectMock
    CreateOrderUseCase createOrder;

    @InjectMock
    GetOrderUseCase getOrder;

    @Test
    void post_validOrder_returns201() {
        Order fakeOrder = Order.builder()
                .id("order-test-001")
                .customerId("cust-1")
                .customerEmail("cust@test.com")
                .country("PE")
                .amount(BigDecimal.valueOf(1200))
                .status(OrderStatus.PENDING)
                .fraudRisk(FraudRisk.UNKNOWN)
                .createdAt(Instant.now())
                .items(List.of(new OrderItem("p1", "Laptop", 1, BigDecimal.valueOf(1200))))
                .build();

        when(createOrder.execute(any())).thenReturn(fakeOrder);

        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerId": "cust-1",
                  "customerEmail": "cust@test.com",
                  "country": "PE",
                  "items": [
                    {"productId": "p1", "productName": "Laptop", "quantity": 1, "price": 1200.00}
                  ]
                }
                """)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("id", equalTo("order-test-001"))
            .body("status", equalTo("PENDING"))
            .body("amount", equalTo(1200.0f));
    }

    @Test
    void post_missingCustomerId_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "customerEmail": "cust@test.com",
                  "country": "PE",
                  "items": [{"productId": "p1", "productName": "X", "quantity": 1, "price": 10}]
                }
                """)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400);
    }

    @Test
    void get_existingOrder_returns200() {
        Order fakeOrder = Order.builder()
                .id("order-abc")
                .customerId("cust-1")
                .customerEmail("cust@test.com")
                .country("PE")
                .amount(BigDecimal.TEN)
                .status(OrderStatus.APPROVED)
                .fraudRisk(FraudRisk.LOW)
                .createdAt(Instant.now())
                .items(List.of())
                .build();

        when(getOrder.findById("order-abc")).thenReturn(Optional.of(fakeOrder));

        given()
        .when()
            .get("/api/orders/order-abc")
        .then()
            .statusCode(200)
            .body("id", equalTo("order-abc"))
            .body("status", equalTo("APPROVED"));
    }

    @Test
    void get_nonExistingOrder_returns404() {
        when(getOrder.findById("ghost-id")).thenReturn(Optional.empty());

        given()
        .when()
            .get("/api/orders/ghost-id")
        .then()
            .statusCode(404);
    }
}
