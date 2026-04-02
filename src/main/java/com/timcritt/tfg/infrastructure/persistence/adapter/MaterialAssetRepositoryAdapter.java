package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialAssetEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialAssetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MaterialAssetRepositoryAdapter {
    private final MaterialAssetJpaRepository repository;

    public Optional<MaterialAsset> findById(Long id) {
        return repository.findById(id).map(MaterialAssetEntityMapper::toDomain);
    }

    public MaterialAsset save(MaterialAsset asset, MaterialNodeEntity nodeEntity) {
        MaterialAssetEntity entity = MaterialAssetEntityMapper.toEntity(asset, nodeEntity);
        return MaterialAssetEntityMapper.toDomain(repository.save(entity));
    }
    // Add more methods as needed
}
