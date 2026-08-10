package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.infrastructure.web.openapi.StandardApiErrorResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private static final long DEFAULT_EXPIRATION_SECONDS = 3600L;

    private final StorageRepositoryPort storageRepositoryPort;

    @Autowired
    public StorageController(StorageRepositoryPort storageRepositoryPort) {
        this.storageRepositoryPort = storageRepositoryPort;
    }

    @GetMapping("/presigned-url")
    @StandardApiErrorResponses
    public ResponseEntity<String> generatePresignedUrl(
            @RequestParam String bucket,
            @RequestParam String objectKey) {
        String url = storageRepositoryPort.generatePresignedUrl(bucket, objectKey, DEFAULT_EXPIRATION_SECONDS);
        return ResponseEntity.ok(url);
    }
}

