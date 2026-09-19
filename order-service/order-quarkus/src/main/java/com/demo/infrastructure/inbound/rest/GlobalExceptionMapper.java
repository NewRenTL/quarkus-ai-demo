package com.demo.infrastructure.inbound.rest;

import com.demo.domain.model.DomainException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof DomainException) {
            // Errores de negocio — 400, mensaje seguro de exponer
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage(), "type", "DOMAIN_ERROR"))
                    .build();
        }

        if (e instanceof jakarta.ws.rs.NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Resource not found", "type", "NOT_FOUND"))
                    .build();
        }

        // Errores inesperados — 500, sin exponer detalles internos
        log.error("Unexpected error processing request", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error", "type", "INTERNAL_ERROR"))
                .build();
    }
}
