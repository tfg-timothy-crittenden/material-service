package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.domain.event.MaterialTitlesUpdatedEvent;
import com.timcritt.tfg.application.port.outbound.MaterialTitlesUpdatedEventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaterialTitlesUpdatedKafkaPublisher implements MaterialTitlesUpdatedEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final boolean enabled;

    public MaterialTitlesUpdatedKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${messaging.topics.material-titles-updated:material.titles.updated.v1}") String topic,
            @Value("${messaging.topics.material-titles-updated.enabled:true}") boolean enabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.enabled = enabled;
    }

    @Override
    public void publishMaterialTitlesUpdated(MaterialTitlesUpdatedEvent event) {
        if (event == null || event.getMaterialId() == null || event.getVersion() == null) {
            throw new IllegalArgumentException("material titles updated event with materialId and version is required");
        }
        if (!enabled) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.getMaterialId()), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize material titles updated event", e);
        }
    }
}


