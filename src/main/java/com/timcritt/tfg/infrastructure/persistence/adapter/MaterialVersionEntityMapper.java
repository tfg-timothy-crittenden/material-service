package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.domain.model.MaterialVersion;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialVersionJpaEntity;

public class MaterialVersionEntityMapper {
    public static MaterialVersion toDomain(MaterialVersionJpaEntity entity) {
        if (entity == null) return null;
        return MaterialVersion.builder()
                .id(entity.getId())
                .materialId(entity.getMaterialId())
                .versionNo(entity.getVersionNo())
                .status(entity.getStatus())
                .changeSummary(entity.getChangeSummary())
                .createdBy(entity.getCreatedBy())
                .publishedAt(entity.getPublishedAt())
                .blueprintSnapshot(entity.getBlueprintSnapshot())
                .isLocked(entity.getIsLocked())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    public static MaterialVersionJpaEntity toEntity(MaterialVersion domain) {
        if (domain == null) return null;
        return MaterialVersionJpaEntity.builder()
                .id(domain.getId())
                .materialId(domain.getMaterialId())
                .versionNo(domain.getVersionNo())
                .status(domain.getStatus())
                .changeSummary(domain.getChangeSummary())
                .createdBy(domain.getCreatedBy())
                .publishedAt(domain.getPublishedAt())
                .blueprintSnapshot(domain.getBlueprintSnapshot())
                .isLocked(domain.getIsLocked())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

