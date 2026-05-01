package com.timcritt.tfg.infrastructure.service.toefl;

import com.timcritt.tfg.application.dto.MaterialNodeWithAssetsResult;
import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.dto.SpeakingSectionSummary;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.port.outbound.MaterialRepositoryPort;
import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingNavigationUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TOEFLSpeakingNavigationServiceAdapter implements TOEFLSpeakingNavigationUseCase {
    private final TOEFLSpeakingNavigationUseCaseService delegate;

    public TOEFLSpeakingNavigationServiceAdapter(
            MaterialRepositoryPort materialRepository,
            MaterialNodeRepositoryPort materialNodeRepository,
            MaterialAssetRepositoryPort materialAssetRepository) {
        this.delegate = new TOEFLSpeakingNavigationUseCaseService(
                materialRepository,
                materialNodeRepository,
                materialAssetRepository
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialNodeWithAssetsResult> getQuestion(Long materialId, int partOrder, int questionOrder) {
        return delegate.getQuestion(materialId, partOrder, questionOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpeakingSectionEditResult> getSpeakingSectionForEdit(Long materialId) {
        return delegate.getSpeakingSectionForEdit(materialId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpeakingSectionSummary> getAllSpeakingSectionSummaries() {
        return delegate.getAllSpeakingSectionSummaries();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpeakingSectionSummary> getDraftSpeakingSectionSummaries() {
        return delegate.getDraftSpeakingSectionSummaries();
    }
}

