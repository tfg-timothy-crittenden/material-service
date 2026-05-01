package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.dto.toefl.MaterialDeletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MaterialDeletionKafkaPublisherTest {

    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void publishMaterialDeleted_sendsJsonPayloadToConfiguredTopic() {
        MaterialDeletionKafkaPublisher publisher = new MaterialDeletionKafkaPublisher(
                kafkaTemplate,
                objectMapper,
                "material.deleted.v1"
        );

        MaterialDeletedEvent event = MaterialDeletedEvent.builder()
                .materialId(42L)
                .rootNodeId(10L)
                .deletedAt(Instant.parse("2026-05-01T12:00:00Z"))
                .build();

        publisher.publishMaterialDeleted(event);

        verify(kafkaTemplate, times(1)).send(
                org.mockito.ArgumentMatchers.eq("material.deleted.v1"),
                org.mockito.ArgumentMatchers.eq("42"),
                org.mockito.ArgumentMatchers.contains("\"materialId\":42")
        );
    }
}

