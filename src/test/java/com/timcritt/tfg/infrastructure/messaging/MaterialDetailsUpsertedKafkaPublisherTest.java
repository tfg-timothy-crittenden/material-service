package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MaterialDetailsUpsertedKafkaPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void publishMaterialDetailsUpserted_sendsJsonPayloadAndRequestHeader() {
        MaterialDetailsUpsertedKafkaPublisher publisher = new MaterialDetailsUpsertedKafkaPublisher(
                kafkaTemplate,
                objectMapper,
                "material.details.upserted.v1"
        );

        MaterialDetailsUpsertedEvent event = MaterialDetailsUpsertedEvent.builder()
                .materialId(42L)
                .version(7L)
                .materialTitle("New Material")
                .part1Title("New Part 1")
                .part2Title("New Part 2")
                .description("Updated description")
                .updatedAt(Instant.parse("2026-09-01T12:00:00Z"))
                .build();

        publisher.publishMaterialDetailsUpserted(event, "request-abc");

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<ProducerRecord<String, String>> recordCaptor =
                (org.mockito.ArgumentCaptor<ProducerRecord<String, String>>) (org.mockito.ArgumentCaptor<?>) org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, String> record = recordCaptor.getValue();
        assertThat(record.topic()).isEqualTo("material.details.upserted.v1");
        assertThat(record.key()).isEqualTo("42");
        assertThat(record.value())
                .contains("\"materialId\":42")
                .contains("\"version\":7")
                .contains("\"materialTitle\":\"New Material\"")
                .contains("\"part1Title\":\"New Part 1\"")
                .contains("\"part2Title\":\"New Part 2\"")
                .contains("\"description\":\"Updated description\"")
                .contains("\"updatedAt\":");
        assertThat(record.headers().lastHeader("x-request-id")).isNotNull();
        assertThat(new String(record.headers().lastHeader("x-request-id").value(), StandardCharsets.UTF_8)).isEqualTo("request-abc");
    }
}

