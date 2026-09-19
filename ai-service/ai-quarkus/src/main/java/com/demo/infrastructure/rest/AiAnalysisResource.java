package com.demo.infrastructure.rest;

import com.demo.ai.FraudDetectionAI;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Path("/api/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiAnalysisResource {

    @Inject
    FraudDetectionAI fraudDetectionAI;

    @POST
    @Path("/analyze")
    @Operation(summary = "Analyze an order for fraud using Gemini 2.5 Flash")
    public Uni<String> analyze(AnalyzeRequest request) {
        int hour = Instant.now().atZone(java.time.ZoneOffset.UTC).getHour();

        log.info("Direct fraud analysis requested for order {} from {}",
                request.orderId(), request.country());

        return Uni.createFrom().item(() -> fraudDetectionAI.analyzeForFraud(
                request.orderId(),
                request.amount(),
                request.country(),
                hour,
                request.customerEmail(),
                request.itemCount(),
                request.customerHistory()
        ));
    }

    public record AnalyzeRequest(
            String orderId,
            String amount,
            String country,
            String customerEmail,
            int itemCount,
            String customerHistory
    ) {}
}
