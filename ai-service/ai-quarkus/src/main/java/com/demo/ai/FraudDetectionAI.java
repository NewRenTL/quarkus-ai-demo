package com.demo.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface FraudDetectionAI {

    @SystemMessage("""
            Eres un sistema experto en detección de fraude para e-commerce.
            Analiza la transacción y responde ÚNICAMENTE con JSON válido, sin texto adicional:
            {
              "riskScore": <número entre 0 y 100>,
              "riskLevel": "<LOW | MEDIUM | HIGH | CRITICAL>",
              "reasons": ["<razón 1>", "<razón 2>"],
              "recommendation": "<APPROVE | REVIEW | REJECT>",
              "confidence": <número entre 0.0 y 1.0>
            }

            Criterios de evaluación:
            - Montos > $5000 desde países de alto riesgo: CRITICAL
            - Hora inusual (2am-5am) + monto alto: HIGH
            - País diferente al historial del cliente: MEDIUM
            - Todo normal: LOW
            """)
    @UserMessage("""
            Analiza esta transacción:
            - Orden ID: {orderId}
            - Monto: ${amount}
            - País: {country}
            - Hora (UTC): {hour}:00
            - Email cliente: {customerEmail}
            - Cantidad de items: {itemCount}
            - Historial del cliente: {customerHistory}
            """)
    String analyzeForFraud(
            @V("orderId") String orderId,
            @V("amount") String amount,
            @V("country") String country,
            @V("hour") int hour,
            @V("customerEmail") String customerEmail,
            @V("itemCount") int itemCount,
            @V("customerHistory") String customerHistory
    );
}
