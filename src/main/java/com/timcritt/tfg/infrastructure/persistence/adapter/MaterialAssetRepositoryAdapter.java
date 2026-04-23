package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.MaterialAssetRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialAssetEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialNodeJpaRepository;
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
    private final MaterialNodeJpaRepository nodeRepository;

    @Override
    public Optional<MaterialAsset> findById(Long id) {
        return repository.findById(id).map(MaterialAssetEntityMapper::toDomain);
    }

    @Override
    public MaterialAsset save(MaterialAsset asset) {
        // Fetch managed MaterialNodeJpaEntity
        MaterialNodeJpaEntity nodeEntity = nodeRepository.findById(asset.getMaterialNodeId())
            .orElseThrow(() -> new IllegalArgumentException("MaterialNode not found for id: " + asset.getMaterialNodeId()));
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
