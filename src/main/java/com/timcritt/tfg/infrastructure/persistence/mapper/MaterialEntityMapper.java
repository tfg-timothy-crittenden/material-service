package com.timcritt.tfg.infrastructure.persistence.mapper;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;

public class MaterialEntityMapper {
    public static Material toDomain(MaterialJpaEntity entity) {
        if (entity == null) return null;
        return Material.builder()
                .id(entity.getId())
                .examFamilyId(entity.getExamFamilyId())
                .blueprintId(entity.getBlueprintId())
                .code(entity.getCode())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .authorId(entity.getAuthorId())
                .ownerOrgId(entity.getOwnerOrgId())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    public static MaterialJpaEntity toEntity(Material domain) {
        if (domain == null) return null;
        return MaterialJpaEntity.builder()
                .id(domain.getId())
                .examFamilyId(domain.getExamFamilyId())
                .blueprintId(domain.getBlueprintId())
                .code(domain.getCode())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .authorId(domain.getAuthorId())
                .ownerOrgId(domain.getOwnerOrgId())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

