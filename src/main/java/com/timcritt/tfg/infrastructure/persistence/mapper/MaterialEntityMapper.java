package com.timcritt.tfg.infrastructure.persistence.mapper;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialStatus;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;

public class MaterialEntityMapper {
    public static Material toDomain(MaterialJpaEntity entity) {
        if (entity == null) return null;
        return Material.builder()
                .id(entity.getId())
                .examFamilyId(entity.getExamFamilyId())
                .materialNodeId(entity.getMaterialNodeId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .authorId(entity.getAuthorId())
                .ownerOrgId(entity.getOwnerOrgId())
                .status(entity.getStatus())
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
                .materialNodeId(domain.getMaterialNodeId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .authorId(domain.getAuthorId())
                .ownerOrgId(domain.getOwnerOrgId())
                // Default to DRAFT if not explicitly set – prevents accidental null.
                .status(domain.getStatus() != null ? domain.getStatus() : MaterialStatus.DRAFT)
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
