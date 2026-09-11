package com.timcritt.tfg.application.integration;

import java.time.Instant;

public record StorageObjectDeleteRequestedMessage(
        Long materialId,
        String bucket,
        String storageKey,
        Instant requestedAt
) {
}

