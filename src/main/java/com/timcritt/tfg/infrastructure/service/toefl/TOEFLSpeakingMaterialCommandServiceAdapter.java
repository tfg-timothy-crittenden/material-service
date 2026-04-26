package com.timcritt.tfg.infrastructure.service.toefl;

import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingPart1UploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingMaterialCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TOEFLSpeakingMaterialCommandServiceAdapter implements TOEFLSpeakingMaterialCommandUseCase {
    private final TOEFLSpeakingMaterialCommandService delegate;

    public TOEFLSpeakingMaterialCommandServiceAdapter(
            MaterialRepositoryPort materialRepository,
            MaterialNodeRepositoryPort materialNodeRepository,
            MaterialAssetRepositoryPort materialAssetRepository,
            StorageRepositoryPort storageRepositoryPort) {
        this.delegate = new TOEFLSpeakingMaterialCommandService(
                materialRepository,
                materialNodeRepository,
                materialAssetRepository,
                storageRepositoryPort
        );
    }

    @Override
    @Transactional
    public void uploadSpeakingPart1(TOEFLSpeakingPart1UploadCommand command) {
        delegate.uploadSpeakingPart1(command);
    }

    @Override
    @Transactional
    public void uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command) {
        delegate.uploadSpeakingSection(command);
    }

    @Override
    @Transactional
    public void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command) {
        delegate.updateSpeakingSection(command);
    }
}

