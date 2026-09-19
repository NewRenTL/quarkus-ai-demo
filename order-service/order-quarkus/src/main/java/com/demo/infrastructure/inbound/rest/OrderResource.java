package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.Order;
import com.demo.domain.port.in.CreateOrderUseCase;
import com.demo.domain.port.in.GetOrderUseCase;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Slf4j
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Order management — Quarkus Reactive")
public class OrderResource {

    @Inject
    CreateOrderUseCase createOrder;

    @Inject
    GetOrderUseCase getOrder;

    @POST
    @Blocking
    @Operation(summary = "Create a new order")
    public Response create(@Valid CreateOrderRequest request) {
        log.debug("Received order request from customer {}", request.getCustomerId());
        Order order = createOrder.execute(request.toCommand());
        return Response.status(Response.Status.CREATED)
                .entity(OrderResponse.from(order))
                .build();
    }

    @GET
    @Path("/{orderId}")
    @Blocking
    @Operation(summary = "Get order by ID")
    public Response getById(@PathParam("orderId") String orderId) {
        return getOrder.findById(orderId)
                .<Response>map(order -> Response.ok(OrderResponse.from(order)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/customer/{customerId}")
    @Blocking
    @Operation(summary = "Get all orders for a customer")
    public List<OrderResponse> getByCustomer(@PathParam("customerId") String customerId) {
        return getOrder.findByCustomer(customerId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
