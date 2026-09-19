package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.OrderItem;
import com.demo.domain.port.in.CreateOrderUseCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @Email
    @NotBlank
    private String customerEmail;

    @NotBlank
    private String country;

    @NotEmpty
    private List<ItemRequest> items;

    public CreateOrderUseCase.CreateOrderCommand toCommand() {
        return new CreateOrderUseCase.CreateOrderCommand(
                customerId,
                customerEmail,
                country,
                items.stream()
                        .map(i -> new OrderItem(i.productId(), i.productName(),
                                i.quantity(), i.price()))
                        .toList()
        );
    }

    public record ItemRequest(
            String productId,
            String productName,
            int quantity,
            @NotNull BigDecimal price
    ) {}
}
