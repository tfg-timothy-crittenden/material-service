package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.integration.StorageObjectDeleteRequestedMessage;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.kafka.listener", name = "auto-startup", havingValue = "true", matchIfMissing = true)
public class StorageObjectDeleteRequestedKafkaListener {

    private final ObjectMapper objectMapper;
    private final StorageRepositoryPort storageRepositoryPort;
    private final MessagingKafkaProperties messagingKafkaProperties;

    public StorageObjectDeleteRequestedKafkaListener(
            ObjectMapper objectMapper,
            StorageRepositoryPort storageRepositoryPort,
            MessagingKafkaProperties messagingKafkaProperties
    ) {
        this.objectMapper = objectMapper;
        this.storageRepositoryPort = storageRepositoryPort;
        this.messagingKafkaProperties = messagingKafkaProperties;
    }

    @KafkaListener(
            topics = "${material.kafka.storage-object-delete-requested-topic:storage.object.delete.requested.v1}",
            groupId = "${material.kafka.storage-object-delete-requested-group-id:material-service-storage-cleanup}",
            containerFactory = "storageCleanupKafkaListenerContainerFactory"
    )
    public void onMessage(String payload) {
        if (!messagingKafkaProperties.isEnabled()) {
            return;
        }

        StorageObjectDeleteRequestedMessage message = deserialize(payload);
        validate(message);

        storageRepositoryPort.deleteObject(message.bucket(), message.storageKey());
    }

    private StorageObjectDeleteRequestedMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, StorageObjectDeleteRequestedMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Malformed storage cleanup message", e);
        }
    }

    private void validate(StorageObjectDeleteRequestedMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Storage cleanup message is required");
        }
        if (message.materialId() == null) {
            throw new IllegalArgumentException("Storage cleanup message materialId is required");
        }
        if (!hasText(message.bucket())) {
            throw new IllegalArgumentException("Storage cleanup message bucket is required");
        }
        if (!hasText(message.storageKey())) {
            throw new IllegalArgumentException("Storage cleanup message storageKey is required");
        }
        if (message.requestedAt() == null) {
            throw new IllegalArgumentException("Storage cleanup message requestedAt is required");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

