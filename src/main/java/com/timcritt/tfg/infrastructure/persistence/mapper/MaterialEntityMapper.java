package com.timcritt.tfg.infrastructure.persistence.mapper;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;

public class MaterialEntityMapper {
    public static Material toDomain(MaterialJpaEntity entity) {
        if (entity == null) return null;
        Material material = Material.builder()
                .id(entity.getId())
                .examFamilyId(entity.getExamFamilyId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .authorId(entity.getAuthorId())
                .ownerOrgId(entity.getOwnerOrgId())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        if (entity.getMaterialNodeId() != null) {
            material.attachRoot(MaterialNode.builder()
                    .id(entity.getMaterialNodeId())
                    .materialId(entity.getId())
                    .build());
        }
        return material;
    }

    public static MaterialJpaEntity toEntity(Material material) {
        if (material == null) {
            return null;
        }
        return MaterialJpaEntity.builder()
                .id(material.getId())
                .examFamilyId(material.getExamFamilyId())
                .materialNodeId(material.getRootId())
                .title(material.getTitle())
                .description(material.getDescription())
                .authorId(material.getAuthorId())
                .ownerOrgId(material.getOwnerOrgId())
                .status(material.getStatus())
                .version(material.getVersion())
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }
}
