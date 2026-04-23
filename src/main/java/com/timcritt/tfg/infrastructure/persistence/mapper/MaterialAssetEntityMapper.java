package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;

public class MaterialAssetEntityMapper {
    public static MaterialAsset toDomain(MaterialAssetEntity entity) {
        if (entity == null) return null;
        MaterialAsset asset = new MaterialAsset();
        asset.setId(entity.getId());
        asset.setMaterialNodeId(entity.getMaterialNode() != null ? entity.getMaterialNode().getId() : null);
        asset.setKind(MaterialAsset.Kind.valueOf(entity.getKind().name()));
        asset.setStorageKey(entity.getStorageKey());
        asset.setOriginalFilename(entity.getOriginalFilename());
        asset.setMimeType(entity.getMimeType());
        asset.setFileSizeBytes(entity.getFileSizeBytes());
        asset.setTitle(entity.getTitle());
        asset.setTranscriptText(entity.getTranscriptText());
        asset.setDisplayOrder(entity.getDisplayOrder());
        asset.setMetadata(entity.getMetadata());
        asset.setVersion(entity.getVersion());
        asset.setCreatedAt(entity.getCreatedAt());
        asset.setUpdatedAt(entity.getUpdatedAt());
        return asset;
    }

    public static MaterialAssetEntity toEntity(MaterialAsset domain, MaterialNodeJpaEntity nodeEntity) {
        if (domain == null) return null;
        MaterialAssetEntity entity = new MaterialAssetEntity();
        entity.setId(domain.getId());
        entity.setMaterialNode(nodeEntity);
        entity.setKind(MaterialAssetEntity.Kind.valueOf(domain.getKind().name()));
        entity.setStorageKey(domain.getStorageKey());
        entity.setOriginalFilename(domain.getOriginalFilename());
        entity.setMimeType(domain.getMimeType());
        entity.setFileSizeBytes(domain.getFileSizeBytes());
        entity.setTitle(domain.getTitle());
        entity.setTranscriptText(domain.getTranscriptText());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setMetadata(domain.getMetadata());
        entity.setVersion(domain.getVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
