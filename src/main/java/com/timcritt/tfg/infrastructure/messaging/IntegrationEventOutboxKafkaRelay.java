package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.integration.IntegrationEventTypes;
import com.timcritt.tfg.application.integration.MaterialDetailsUpsertedOutboxMessage;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class IntegrationEventOutboxKafkaRelay {

    private static final String REQUEST_ID_HEADER = "x-request-id";

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String materialDetailsUpsertedTopic;
    private final String materialDeletedTopic;

    public IntegrationEventOutboxKafkaRelay(
            OutboxEventJpaRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${material.kafka.material-details-upserted-topic:material.details.upserted.v1}") String materialDetailsUpsertedTopic,
            @org.springframework.beans.factory.annotation.Value("${messaging.topics.material-deleted:material.deleted.v1}") String materialDeletedTopic) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.materialDetailsUpsertedTopic = materialDetailsUpsertedTopic;
        this.materialDeletedTopic = materialDeletedTopic;
    }

    @Scheduled(fixedDelay = 5000)
    public void relayPendingEvents() {
        List<OutboxEventJpaEntity> events = outboxRepository.findAllByOrderByOccurredAtAsc();
        for (OutboxEventJpaEntity entity : events) {
            relay(entity);
        }
    }

    private void relay(OutboxEventJpaEntity entity) {
        if (entity == null || entity.getEventType() == null) {
            return;
        }

        try {
            if (IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED.equals(entity.getEventType())) {
                relayMaterialDetailsUpserted(entity);
            } else if (IntegrationEventTypes.MATERIAL_DELETED.equals(entity.getEventType())) {
                relayMaterialDeleted(entity);
            } else {
                log.warn("Skipping unsupported outbox event type: id={}, eventType={}", entity.getId(), entity.getEventType());
                return;
            }

            outboxRepository.deleteById(entity.getId());
        } catch (Exception ex) {
            log.error("Failed to relay outbox event: id={}, eventType={}", entity.getId(), entity.getEventType(), ex);
        }
    }

    private void relayMaterialDetailsUpserted(OutboxEventJpaEntity entity) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(entity.getPayload());
        MaterialDetailsUpsertedEvent event;
        String requestId = null;

        if (root.hasNonNull("event")) {
            MaterialDetailsUpsertedOutboxMessage envelope = objectMapper.treeToValue(root, MaterialDetailsUpsertedOutboxMessage.class);
            event = envelope.getEvent();
            requestId = envelope.getRequestId();
        } else {
            event = objectMapper.treeToValue(root, MaterialDetailsUpsertedEvent.class);
        }

        if (event == null || event.getMaterialId() == null || event.getVersion() == null) {
            throw new IllegalArgumentException("material details upserted outbox payload is invalid");
        }

        log.info("Publishing material details upserted event from outbox: topic={}, materialId={}, version={}, requestId={}",
                materialDetailsUpsertedTopic,
                event.getMaterialId(),
                event.getVersion(),
                requestId);

        ProducerRecord<String, String> record = new ProducerRecord<>(materialDetailsUpsertedTopic, String.valueOf(event.getMaterialId()), objectMapper.writeValueAsString(event));
        if (requestId != null && !requestId.trim().isEmpty()) {
            record.headers().add(REQUEST_ID_HEADER, requestId.trim().getBytes(StandardCharsets.UTF_8));
        }
        kafkaTemplate.send(record);
    }

    private void relayMaterialDeleted(OutboxEventJpaEntity entity) throws JsonProcessingException {
        MaterialDeletedEvent event = objectMapper.readValue(entity.getPayload(), MaterialDeletedEvent.class);
        if (event.getMaterialId() == null) {
            throw new IllegalArgumentException("material deleted outbox payload is invalid");
        }

        log.info("Publishing material deleted event from outbox: topic={}, materialId={}",
                materialDeletedTopic,
                event.getMaterialId());

        kafkaTemplate.send(materialDeletedTopic, String.valueOf(event.getMaterialId()), objectMapper.writeValueAsString(event));
    }
}


