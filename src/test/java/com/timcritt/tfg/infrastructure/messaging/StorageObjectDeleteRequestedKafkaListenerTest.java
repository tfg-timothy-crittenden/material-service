package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.integration.StorageObjectDeleteRequestedMessage;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class StorageObjectDeleteRequestedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final StorageRepositoryPort storageRepositoryPort = mock(StorageRepositoryPort.class);

    @Test
    void validDeleteRequest_deletesStorageObjectExactlyOnce() throws Exception {
        String storageKey = "speaking/91/part1/audio/question_1/old.mp3";

        StorageObjectDeleteRequestedKafkaListener listener = new StorageObjectDeleteRequestedKafkaListener(
                objectMapper,
                storageRepositoryPort,
                enabledMessagingKafkaProperties()
        );

        String payload = objectMapper.writeValueAsString(new StorageObjectDeleteRequestedMessage(
                91L,
                "toefl",
                storageKey,
                Instant.parse("2026-09-11T12:00:00Z")
        ));

        listener.onMessage(payload);


        verify(storageRepositoryPort, times(1)).deleteObject("toefl", storageKey);
    }

    @Test
    void storageDeletionException_propagates() throws Exception {
        String storageKey = "speaking/91/part1/audio/question_1/old.mp3";
        RuntimeException failure = new RuntimeException("minio down");
        doThrow(failure).when(storageRepositoryPort).deleteObject("toefl", storageKey);

        StorageObjectDeleteRequestedKafkaListener listener = new StorageObjectDeleteRequestedKafkaListener(
                objectMapper,
                storageRepositoryPort,
                enabledMessagingKafkaProperties()
        );

        String payload = objectMapper.writeValueAsString(new StorageObjectDeleteRequestedMessage(
                91L,
                "toefl",
                storageKey,
                Instant.parse("2026-09-11T12:00:00Z")
        ));

        assertThatThrownBy(() -> listener.onMessage(payload))
                .isSameAs(failure);

        verify(storageRepositoryPort, times(1)).deleteObject("toefl", storageKey);
    }

    @Test
    void malformedOrInvalidPayload_throwsAndDoesNotDeleteStorage() {
        StorageObjectDeleteRequestedKafkaListener listener = new StorageObjectDeleteRequestedKafkaListener(
                objectMapper,
                storageRepositoryPort,
                enabledMessagingKafkaProperties()
        );

        assertThatThrownBy(() -> listener.onMessage("{"))
                .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(storageRepositoryPort);

        String missingStorageKeyPayload = """
                {"materialId":91,"bucket":"toefl","requestedAt":"2026-09-11T12:00:00Z"}
                """;

        assertThatThrownBy(() -> listener.onMessage(missingStorageKeyPayload))
                .isInstanceOf(RuntimeException.class);
        verify(storageRepositoryPort, never()).deleteObject("toefl", null);
    }

    private static MessagingKafkaProperties enabledMessagingKafkaProperties() {
        MessagingKafkaProperties properties = new MessagingKafkaProperties();
        properties.setEnabled(true);
        return properties;
    }
}

