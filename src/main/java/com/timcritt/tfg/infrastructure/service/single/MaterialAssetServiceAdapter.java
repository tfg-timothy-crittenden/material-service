package com.timcritt.tfg.infrastructure.service.single;

import com.timcritt.tfg.application.port.inbound.MaterialAssetUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.application.service.single.MaterialAssetUseCaseService;
import com.timcritt.tfg.domain.model.MaterialAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialAssetServiceAdapter implements MaterialAssetUseCase {
    private final MaterialAssetUseCaseService delegate;

    public MaterialAssetServiceAdapter(MaterialAssetRepositoryPort repositoryPort) {
        this.delegate = new MaterialAssetUseCaseService(repositoryPort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialAsset> getById(Long id) {
        return delegate.getById(id);
    }

    @Override
    @Transactional
    public MaterialAsset save(MaterialAsset asset) {
        return delegate.save(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialAsset> findByMaterialNodeId(Long materialNodeId) {
        return delegate.findByMaterialNodeId(materialNodeId);
    }
}

