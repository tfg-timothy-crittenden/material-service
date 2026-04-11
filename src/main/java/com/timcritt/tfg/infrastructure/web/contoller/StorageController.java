package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private final StorageRepositoryPort storageRepositoryPort;

    @Autowired
    public StorageController(StorageRepositoryPort storageRepositoryPort) {
        this.storageRepositoryPort = storageRepositoryPort;
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(
            @RequestParam String bucket,
            @RequestParam String objectKey,
            @RequestParam(defaultValue = "3600") long expirationSeconds) {
        String url = storageRepositoryPort.generatePresignedUrl(bucket, objectKey, expirationSeconds);
        return ResponseEntity.ok(url);
    }
}

