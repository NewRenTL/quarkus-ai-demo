package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.*;
import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreateOrderUseCase createOrder;
    @MockBean GetOrderUseCase getOrder;

    @Test
    void post_validOrder_returns201() throws Exception {
        Order fakeOrder = Order.builder()
                .id("order-spring-001")
                .customerId("cust-1")
                .customerEmail("cust@test.com")
                .country("PE")
                .amount(BigDecimal.valueOf(999))
                .status(OrderStatus.PENDING)
                .fraudRisk(FraudRisk.UNKNOWN)
                .createdAt(Instant.now())
                .items(List.of(new OrderItem("p1", "Book", 1, BigDecimal.valueOf(999))))
                .build();

        when(createOrder.execute(any())).thenReturn(fakeOrder);

        String body = """
                {
                  "customerId": "cust-1",
                  "customerEmail": "cust@test.com",
                  "country": "PE",
                  "items": [{"productId": "p1", "productName": "Book", "quantity": 1, "price": 999}]
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order-spring-001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void post_invalidEmail_returns400() throws Exception {
        String body = """
                {
                  "customerId": "cust-1",
                  "customerEmail": "not-an-email",
                  "country": "PE",
                  "items": [{"productId": "p1", "productName": "X", "quantity": 1, "price": 10}]
                }
                """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_existingOrder_returns200() throws Exception {
        Order fakeOrder = Order.builder()
                .id("order-abc")
                .customerId("cust-1")
                .customerEmail("c@t.com")
                .country("PE")
                .amount(BigDecimal.TEN)
                .status(OrderStatus.APPROVED)
                .fraudRisk(FraudRisk.LOW)
                .createdAt(Instant.now())
                .items(List.of())
                .build();

        when(getOrder.findById("order-abc")).thenReturn(Optional.of(fakeOrder));

        mockMvc.perform(get("/api/orders/order-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-abc"))
                .andExpect(jsonPath("$.fraudRisk").value("LOW"));
    }

    @Test
    void get_nonExistingOrder_returns404() throws Exception {
        when(getOrder.findById("ghost")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/orders/ghost"))
                .andExpect(status().isNotFound());
    }
}
