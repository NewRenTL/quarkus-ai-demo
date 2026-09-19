package com.demo.notification;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Slf4j
@Path("/api/notifications")
public class NotificationResource {

    @Inject
    @Channel("ai-analysis-in")
    Multi<String> analysisStream;

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<String> streamAnalysis() {
        log.info("New SSE client connected to analysis stream");

        return analysisStream
                .onItem().invoke(event -> log.debug("Pushing to SSE client: {}", event))
                .onFailure().invoke(e -> log.error("Stream error", e))
                .onFailure().recoverWithItem("{\"error\": \"stream interrupted\"}");
    }

    @GET
    @Path("/health-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> heartbeat() {
        return Multi.createFrom().ticks()
                .every(java.time.Duration.ofSeconds(5))
                .map(tick -> "heartbeat-" + tick);
    }
}
