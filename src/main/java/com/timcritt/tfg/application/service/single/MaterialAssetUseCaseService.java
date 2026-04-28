package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.MaterialAssetUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialAsset;

import java.util.List;
import java.util.Optional;

public class MaterialAssetUseCaseService implements MaterialAssetUseCase {
    private final MaterialAssetRepositoryPort repository;

    public MaterialAssetUseCaseService(MaterialAssetRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MaterialAsset> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public MaterialAsset save(MaterialAsset asset) {
        return repository.save(asset);
    }

    @Override
    public List<MaterialAsset> findByMaterialNodeId(Long materialNodeId) {
        return repository.findByMaterialNodeId(materialNodeId);
    }
}
