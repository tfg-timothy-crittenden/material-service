package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.outbound.MaterialDetailsUpsertedEventPublisherPort;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MaterialDetailsUpsertedKafkaPublisher implements MaterialDetailsUpsertedEventPublisherPort {

    private static final String REQUEST_ID_HEADER = "x-request-id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public MaterialDetailsUpsertedKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${material.kafka.material-details-upserted-topic:material.details.upserted.v1}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publishMaterialDetailsUpserted(MaterialDetailsUpsertedEvent event, String requestId) {
        if (event == null || event.getMaterialId() == null || event.getVersion() == null) {
            throw new IllegalArgumentException("material details upserted event with materialId and version is required");
        }

        try {
            log.info("Publishing material details upserted event: topic={}, materialId={}, version={}, requestId={}",
                    topic,
                    event.getMaterialId(),
                    event.getVersion(),
                    trimToNull(requestId));
            String payload = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, String.valueOf(event.getMaterialId()), payload);
            if (hasText(requestId)) {
                record.headers().add(REQUEST_ID_HEADER, requestId.trim().getBytes(StandardCharsets.UTF_8));
            }
            kafkaTemplate.send(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize material details upserted event", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

