package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.inbound.MaterialVersionUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialVersionRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialVersion;
import java.util.Optional;

public class MaterialVersionUseCaseService implements MaterialVersionUseCase {
    private final MaterialVersionRepositoryPort repository;
    public MaterialVersionUseCaseService(MaterialVersionRepositoryPort repository) {
        this.repository = repository;
    }
    @Override
    public MaterialVersion createMaterialVersion(MaterialVersion materialVersion) {
        return repository.save(materialVersion);
    }
    @Override
    public Optional<MaterialVersion> findMaterialVersionById(Long id) {
        return repository.findById(id);
    }
    @Override
    public MaterialVersion updateMaterialVersion(MaterialVersion materialVersion) {
        return repository.save(materialVersion);
    }
    @Override
    public Boolean deleteMaterialVersion(Long id) {
        return repository.delete(id);
    }
}

