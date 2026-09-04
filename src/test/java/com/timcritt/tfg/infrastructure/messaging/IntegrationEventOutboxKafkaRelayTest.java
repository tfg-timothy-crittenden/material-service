package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.integration.IntegrationEventTypes;
import com.timcritt.tfg.application.integration.MaterialDetailsUpsertedOutboxMessage;
import com.timcritt.tfg.domain.event.MaterialDeletedEvent;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationEventOutboxKafkaRelayTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final OutboxEventJpaRepository outboxRepository = mock(OutboxEventJpaRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @SuppressWarnings("unchecked")
    void relayPendingEvents_publishesMaterialDetailsUpsertedEnvelopeAndDeletesOutboxRow() throws Exception {
        IntegrationEventOutboxKafkaRelay relay = new IntegrationEventOutboxKafkaRelay(
                outboxRepository,
                kafkaTemplate,
                objectMapper,
                "material.details.upserted.v1",
                "material.deleted.v1"
        );

        OutboxEventJpaEntity entity = outbox(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                IntegrationEventTypes.MATERIAL_DETAILS_UPSERTED,
                objectMapper.writeValueAsString(MaterialDetailsUpsertedOutboxMessage.builder()
                        .requestId("request-abc")
                        .event(MaterialDetailsUpsertedEvent.builder()
                                .materialId(42L)
                                .version(7L)
                                .materialTitle("New Material")
                                .part1Title("New Part 1")
                                .part2Title("New Part 2")
                                .description("Updated description")
                                .updatedAt(Instant.parse("2026-09-01T20:54:10Z"))
                                .build())
                        .build()));

        when(outboxRepository.findAllByOrderByOccurredAtAsc()).thenReturn(List.of(entity));

        relay.relayPendingEvents();

        org.mockito.ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, String> record = recordCaptor.getValue();
        assertThat(record.topic()).isEqualTo("material.details.upserted.v1");
        assertThat(record.key()).isEqualTo("42");
        assertThat(record.value()).contains("\"materialId\":42").contains("\"version\":7");
        assertThat(record.headers().lastHeader("x-request-id")).isNotNull();
        assertThat(new String(record.headers().lastHeader("x-request-id").value())).isEqualTo("request-abc");
        verify(outboxRepository, times(1)).deleteById(entity.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void relayPendingEvents_publishesMaterialDeletedEventAndDeletesOutboxRow() throws Exception {
        IntegrationEventOutboxKafkaRelay relay = new IntegrationEventOutboxKafkaRelay(
                outboxRepository,
                kafkaTemplate,
                objectMapper,
                "material.details.upserted.v1",
                "material.deleted.v1"
        );

        OutboxEventJpaEntity entity = outbox(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                IntegrationEventTypes.MATERIAL_DELETED,
                objectMapper.writeValueAsString(MaterialDeletedEvent.builder()
                        .materialId(77L)
                        .rootNodeId(10L)
                        .deletedAt(Instant.parse("2026-09-01T20:55:00Z"))
                        .build()));

        when(outboxRepository.findAllByOrderByOccurredAtAsc()).thenReturn(List.of(entity));

        relay.relayPendingEvents();

        org.mockito.ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, String> record = recordCaptor.getValue();
        assertThat(record.topic()).isEqualTo("material.deleted.v1");
        assertThat(record.key()).isEqualTo("77");
        assertThat(record.value()).contains("\"materialId\":77").contains("\"rootNodeId\":10");
        assertThat(record.headers().lastHeader("x-request-id")).isNull();
        verify(outboxRepository, times(1)).deleteById(entity.getId());
    }

    private static OutboxEventJpaEntity outbox(UUID id, String eventType, String payload) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(id);
        entity.setAggregateType("Material");
        entity.setAggregateId("42");
        entity.setEventType(eventType);
        entity.setPayload(payload);
        entity.setOccurredAt(Instant.parse("2026-09-01T20:54:00Z"));
        return entity;
    }
}


