package com.timcritt.tfg.infrastructure.service.toefl;

import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.outbound.IntegrationEventOutboxPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageCleanupPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingMaterialCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TOEFLSpeakingMaterialCommandServiceAdapter implements TOEFLSpeakingMaterialCommandUseCase {
    private final TOEFLSpeakingMaterialCommandService delegate;

    public TOEFLSpeakingMaterialCommandServiceAdapter(
            MaterialRepositoryPort materialRepository,
            StorageRepositoryPort storageRepositoryPort,
            StorageCleanupPort storageCleanupPort,
            IntegrationEventOutboxPort outboxPort) {
        this.delegate = new TOEFLSpeakingMaterialCommandService(
                materialRepository,
                storageRepositoryPort,
                storageCleanupPort,
                outboxPort,
                UUID::randomUUID
        );
    }


    @Override
    @Transactional
    public Long uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command) {
        return delegate.uploadSpeakingSection(command);
    }

    @Override
    @Transactional
    public void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command) {
        delegate.updateSpeakingSection(command);
    }

    @Override
    @Transactional
    public void publishSpeakingSection(Long materialId) {
        delegate.publishSpeakingSection(materialId);
    }

    @Override
    @Transactional
    public void deleteSpeakingSection(Long materialId) {
        delegate.deleteSpeakingSection(materialId);
    }
}

