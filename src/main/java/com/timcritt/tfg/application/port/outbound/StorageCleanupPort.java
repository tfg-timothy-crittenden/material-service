package com.timcritt.tfg.application.port.outbound;

public interface StorageCleanupPort {
    void requestDeletion(Long materialId, String bucket, String storageKey);
}

