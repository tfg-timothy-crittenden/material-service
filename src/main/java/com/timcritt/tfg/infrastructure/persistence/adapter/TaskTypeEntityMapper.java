package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.domain.model.TaskType;
import com.timcritt.tfg.infrastructure.persistence.jpa.TaskTypeJpaEntity;

public class TaskTypeEntityMapper {
    public static TaskType toDomain(TaskTypeJpaEntity entity) {
        if (entity == null) return null;
        TaskType taskType = new TaskType();
        taskType.setId(entity.getId());
        taskType.setCode(entity.getCode());
        taskType.setName(entity.getName());
        taskType.setExamFamilyId(entity.getExamFamilyId());
        taskType.setSkillId(entity.getSkillId());
        taskType.setDescription(entity.getDescription());
        taskType.setConfigSchema(entity.getConfigSchema());
        taskType.setVersion(entity.getVersion());
        taskType.setCreatedAt(entity.getCreatedAt());
        taskType.setUpdatedAt(entity.getUpdatedAt());
        return taskType;
    }

    public static TaskTypeJpaEntity toEntity(TaskType domain) {
        if (domain == null) return null;
        return TaskTypeJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .examFamilyId(domain.getExamFamilyId())
                .skillId(domain.getSkillId())
                .description(domain.getDescription())
                .configSchema(domain.getConfigSchema())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

