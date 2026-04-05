package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.MaterialAssetUseCase;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.adapter.MaterialAssetRepositoryAdapter;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MaterialAssetUseCaseService implements MaterialAssetUseCase {
    @Autowired
    private MaterialAssetRepositoryAdapter repositoryAdapter;

    @Override
    public Optional<MaterialAsset> getById(Long id) {
        return repositoryAdapter.findById(id);
    }

    @Override
    public MaterialAsset save(MaterialAsset asset) {
        // You need to fetch MaterialNodeEntity by asset.getMaterialNodeId() in real code
        MaterialNodeEntity nodeEntity = new MaterialNodeEntity();
        nodeEntity.setId(asset.getMaterialNodeId());
        return repositoryAdapter.save(asset, nodeEntity);
    }
}

