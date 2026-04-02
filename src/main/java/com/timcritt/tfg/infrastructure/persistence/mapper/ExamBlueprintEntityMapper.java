package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.ExamBlueprint;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintJpaEntity;

public class ExamBlueprintEntityMapper {
    public static ExamBlueprint toDomain(ExamBlueprintJpaEntity entity) {
        if (entity == null) return null;
        return ExamBlueprint.builder()
                .id(entity.getId())
                .examFamilyId(entity.getExamFamilyId())
                .code(entity.getCode())
                .name(entity.getName())
                .versionNo(entity.getVersionNo())
                .isActive(entity.getIsActive())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ExamBlueprintJpaEntity toEntity(ExamBlueprint domain) {
        if (domain == null) return null;
        return ExamBlueprintJpaEntity.builder()
                .id(domain.getId())
                .examFamilyId(domain.getExamFamilyId())
                .code(domain.getCode())
                .name(domain.getName())
                .versionNo(domain.getVersionNo())
                .isActive(domain.getIsActive())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

