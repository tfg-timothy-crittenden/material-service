package com.timcritt.tfg.infrastructure.storage;

import com.timcritt.tfg.application.integration.StorageObjectDeleteRequestedMessage;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.StorageCleanupPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

import static com.timcritt.tfg.application.integration.IntegrationEventTypes.STORAGE_OBJECT_DELETE_REQUESTED;

@Component
public class OutboxStorageCleanupAdapter implements StorageCleanupPort {

    private final IntegrationEventOutboxPort outboxPort;

    public OutboxStorageCleanupAdapter(IntegrationEventOutboxPort outboxPort) {
        this.outboxPort = outboxPort;
    }

    @Override
    public void requestDeletion(Long materialId, String bucket, String storageKey) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("requestDeletion requires an active transaction");
        }

        outboxPort.append(
                UUID.randomUUID(),
                "Material",
                String.valueOf(materialId),
                STORAGE_OBJECT_DELETE_REQUESTED,
                new StorageObjectDeleteRequestedMessage(
                        materialId,
                        bucket,
                        storageKey,
                        Instant.now()
                )
        );
    }
}

