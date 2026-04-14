package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialAssetEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Repository
@RequiredArgsConstructor
public class MaterialAssetRepositoryAdapter implements MaterialAssetRepositoryPort {
    private final MaterialAssetJpaRepository repository;

    @Override
    public Optional<MaterialAsset> findById(Long id) {
        return repository.findById(id).map(MaterialAssetEntityMapper::toDomain);
    }

    @Override
    public MaterialAsset save(MaterialAsset asset) {
        // This method assumes you have a way to get the MaterialNodeEntity for the asset
        // For now, this is a placeholder; adapt as needed for your actual logic
        MaterialNodeEntity nodeEntity = new MaterialNodeEntity();
        nodeEntity.setId(asset.getMaterialNodeId());
        return MaterialAssetEntityMapper.toDomain(repository.save(MaterialAssetEntityMapper.toEntity(asset, nodeEntity)));
    }

    @Override
    public List<MaterialAsset> findByMaterialNodeId(Long materialNodeId) {
        return repository.findByMaterialNode_Id(materialNodeId)
                .stream()
                .map(MaterialAssetEntityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    // Add more methods as needed
}
