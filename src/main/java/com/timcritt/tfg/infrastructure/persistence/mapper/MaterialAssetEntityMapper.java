package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;

public class MaterialAssetEntityMapper {
    public static MaterialAsset toDomain(MaterialAssetEntity entity) {
        if (entity == null) return null;
        return MaterialAsset.builder()
                .id(entity.getId())
                .materialNodeId(entity.getMaterialNode() != null ? entity.getMaterialNode().getId() : null)
                .kind(MaterialAsset.Kind.valueOf(entity.getKind().name()))
                .storageKey(entity.getStorageKey())
                .originalFilename(entity.getOriginalFilename())
                .mimeType(entity.getMimeType())
                .fileSizeBytes(entity.getFileSizeBytes())
                .title(entity.getTitle())
                .transcriptText(entity.getTranscriptText())
                .displayOrder(entity.getDisplayOrder())
                .metadata(entity.getMetadata())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
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
