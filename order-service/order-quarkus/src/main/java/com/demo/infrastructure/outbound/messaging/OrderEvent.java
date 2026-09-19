package com.demo.infrastructure.outbound.messaging;

import java.math.BigDecimal;

public record OrderEvent(
        String eventType,
        String orderId,
        String customerId,
        BigDecimal amount,
        String country,
        String status
) {}
