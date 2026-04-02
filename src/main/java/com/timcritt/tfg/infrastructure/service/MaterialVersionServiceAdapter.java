package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.MaterialVersionUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialVersionRepositoryPort;
import com.timcritt.tfg.application.service.MaterialVersionUseCaseService;
import com.timcritt.tfg.domain.model.MaterialVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MaterialVersionServiceAdapter implements MaterialVersionUseCase {
    private final MaterialVersionUseCaseService delegate;

    public MaterialVersionServiceAdapter(MaterialVersionRepositoryPort repository) {
        this.delegate = new MaterialVersionUseCaseService(repository);
    }

    @Override
    @Transactional
    public MaterialVersion createMaterialVersion(MaterialVersion materialVersion) {
        return delegate.createMaterialVersion(materialVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialVersion> findMaterialVersionById(Long id) {
        return delegate.findMaterialVersionById(id);
    }

    @Override
    @Transactional
    public MaterialVersion updateMaterialVersion(MaterialVersion materialVersion) {
        return delegate.updateMaterialVersion(materialVersion);
    }

    @Override
    @Transactional
    public Boolean deleteMaterialVersion(Long id) {
        return delegate.deleteMaterialVersion(id);
    }
}

