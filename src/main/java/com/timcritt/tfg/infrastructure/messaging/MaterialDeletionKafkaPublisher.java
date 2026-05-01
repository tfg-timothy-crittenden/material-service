package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.dto.toefl.MaterialDeletedEvent;
import com.timcritt.tfg.application.port.outbound.MaterialDeletionEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaterialDeletionKafkaPublisher implements MaterialDeletionEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public MaterialDeletionKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${messaging.topics.material-deleted:material.deleted.v1}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publishMaterialDeleted(MaterialDeletedEvent event) {
        if (event == null || event.getMaterialId() == null) {
            throw new IllegalArgumentException("material deletion event with materialId is required");
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.getMaterialId()), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize material deletion event", e);
        }
    }
}

