package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintNodeJpaEntity;
import com.timcritt.tfg.domain.model.ExamBlueprintNode;

public class ExamBlueprintNodeEntityMapper {
    public static ExamBlueprintNode toDomain(ExamBlueprintNodeJpaEntity entity) {
        if (entity == null) return null;
        return ExamBlueprintNode.builder()
                .id(entity.getId())
                .blueprintId(entity.getBlueprintId())
                .parentNodeId(entity.getParentNodeId())
                .kind(entity.getKind())
                .code(entity.getCode())
                .title(entity.getTitle())
                .displayOrder(entity.getDisplayOrder())
                .skillId(entity.getSkillId())
                .taskTypeId(entity.getTaskTypeId())
                .isRequired(entity.getIsRequired())
                .minChildren(entity.getMinChildren())
                .maxChildren(entity.getMaxChildren())
                .defaultTimeLimitSeconds(entity.getDefaultTimeLimitSeconds())
                .defaultPrepTimeSeconds(entity.getDefaultPrepTimeSeconds())
                .config(entity.getConfig())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ExamBlueprintNodeJpaEntity toEntity(ExamBlueprintNode domain) {
        if (domain == null) return null;
        return ExamBlueprintNodeJpaEntity.builder()
                .id(domain.getId())
                .blueprintId(domain.getBlueprintId())
                .parentNodeId(domain.getParentNodeId())
                .kind(domain.getKind())
                .code(domain.getCode())
                .title(domain.getTitle())
                .displayOrder(domain.getDisplayOrder())
                .skillId(domain.getSkillId())
                .taskTypeId(domain.getTaskTypeId())
                .isRequired(domain.getIsRequired())
                .minChildren(domain.getMinChildren())
                .maxChildren(domain.getMaxChildren())
                .defaultTimeLimitSeconds(domain.getDefaultTimeLimitSeconds())
                .defaultPrepTimeSeconds(domain.getDefaultPrepTimeSeconds())
                .config(domain.getConfig())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
