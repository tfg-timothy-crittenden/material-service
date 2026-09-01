package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.domain.event.MaterialTitlesUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MaterialTitlesUpdatedKafkaPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void publishMaterialTitlesUpdated_sendsJsonPayloadToConfiguredTopic() {
        MaterialTitlesUpdatedKafkaPublisher publisher = new MaterialTitlesUpdatedKafkaPublisher(
                kafkaTemplate,
                objectMapper,
                "material.titles.updated.v1",
                true
        );

        MaterialTitlesUpdatedEvent event = MaterialTitlesUpdatedEvent.builder()
                .materialId(42L)
                .version(7L)
                .materialTitle("New Material")
                .part1Title("New Part 1")
                .part2Title("New Part 2")
                .updatedAt(Instant.parse("2026-05-02T12:00:00Z"))
                .build();

        publisher.publishMaterialTitlesUpdated(event);

        org.mockito.ArgumentCaptor<String> payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(
                org.mockito.ArgumentMatchers.eq("material.titles.updated.v1"),
                org.mockito.ArgumentMatchers.eq("42"),
                payloadCaptor.capture()
        );
        org.assertj.core.api.Assertions.assertThat(payloadCaptor.getValue())
                .contains("\"materialId\":42")
                .contains("\"version\":7");
    }

    @Test
    void publishMaterialTitlesUpdated_skipsSendWhenDisabled() {
        MaterialTitlesUpdatedKafkaPublisher publisher = new MaterialTitlesUpdatedKafkaPublisher(
                kafkaTemplate,
                objectMapper,
                "material.titles.updated.v1",
                false
        );

        MaterialTitlesUpdatedEvent event = MaterialTitlesUpdatedEvent.builder()
                .materialId(42L)
                .version(7L)
                .materialTitle("New Material")
                .part1Title("New Part 1")
                .part2Title("New Part 2")
                .updatedAt(Instant.parse("2026-05-02T12:00:00Z"))
                .build();

        publisher.publishMaterialTitlesUpdated(event);

        verify(kafkaTemplate, times(0)).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}


