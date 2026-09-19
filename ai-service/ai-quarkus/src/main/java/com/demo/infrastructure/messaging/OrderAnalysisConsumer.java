package com.demo.infrastructure.messaging;

import com.demo.ai.FraudAnalysisResult;
import com.demo.ai.FraudDetectionAI;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.time.Instant;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class OrderAnalysisConsumer {

    @Inject FraudDetectionAI fraudDetectionAI;

    @Inject
    @Channel("ai-analysis-out")
    Emitter<String> analysisEmitter;

    @Inject ObjectMapper objectMapper;

    @Incoming("orders-created-in")
    public Uni<Void> analyzeOrder(String payload) {
        // Deserializar una sola vez y pasar el mapa a través del pipeline
        final Map<String, Object> event;
        try {
            //noinspection unchecked
            event = objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Failed to deserialize order event", e);
            return Uni.createFrom().voidItem();
        }

        String orderId = (String) event.get("orderId");
        String amount  = String.valueOf(event.get("amount"));
        String country = (String) event.get("country");
        String email   = (String) event.getOrDefault("customerEmail", "unknown");
        int itemCount  = ((Number) event.getOrDefault("itemCount", 1)).intValue();
        String history = (String) event.getOrDefault("customerHistory", "new customer");
        int hour       = Instant.now().atZone(java.time.ZoneOffset.UTC).getHour();

        log.info("Analyzing order {} from {} for fraud...", orderId, country);

        return fraudDetectionAI.analyzeForFraud(orderId, amount, country, hour, email, itemCount, history)
                .flatMap(aiResponse -> {
                    try {
                        FraudAnalysisResult result = objectMapper.readValue(aiResponse, FraudAnalysisResult.class);

                        log.info("Order {} — Risk: {} (score: {}), Recommendation: {}",
                                orderId, result.getRiskLevel(), result.getRiskScore(), result.getRecommendation());

                        String analysisPayload = objectMapper.writeValueAsString(Map.of(
                                "orderId",         orderId,
                                "riskScore",       result.getRiskScore(),
                                "riskLevel",       result.getRiskLevel(),
                                "recommendation",  result.getRecommendation(),
                                "reasons",         result.getReasons(),
                                "analyzedAt",      Instant.now().toString()
                        ));

                        analysisEmitter.send(analysisPayload);
                        return Uni.createFrom().voidItem();

                    } catch (Exception e) {
                        log.error("Failed to process AI response for order {}", orderId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .onFailure().invoke(e -> log.error("Analysis pipeline failed for order {}", orderId, e))
                .onFailure().recoverWithNull();
    }
}
