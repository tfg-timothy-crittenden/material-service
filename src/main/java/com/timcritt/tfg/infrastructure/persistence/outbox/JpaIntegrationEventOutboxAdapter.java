package com.timcritt.tfg.infrastructure.persistence.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JpaIntegrationEventOutboxAdapter
        implements IntegrationEventOutboxPort {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaIntegrationEventOutboxAdapter(
            OutboxEventJpaRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    ) {
        String payloadJson;

        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event payload",
                    e
            );
        }

        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(eventId);
        entity.setAggregateType(aggregateType);
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(payloadJson);
        entity.setOccurredAt(Instant.now());

        repository.save(entity);
    }
}