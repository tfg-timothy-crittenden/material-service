package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.TaskType;
import com.timcritt.tfg.infrastructure.web.dto.TaskTypeDto;

public class TaskTypeDtoMapper {
    public static TaskTypeDto toDto(TaskType taskType) {
        if (taskType == null) return null;
        return TaskTypeDto.builder()
                .id(taskType.getId())
                .code(taskType.getCode())
                .name(taskType.getName())
                .examFamilyId(taskType.getExamFamilyId())
                .skillId(taskType.getSkillId())
                .description(taskType.getDescription())
                .configSchema(taskType.getConfigSchema())
                .version(taskType.getVersion())
                .createdAt(taskType.getCreatedAt())
                .updatedAt(taskType.getUpdatedAt())
                .build();
    }

    public static TaskType toDomain(TaskTypeDto dto) {
        if (dto == null) return null;
        TaskType taskType = new TaskType();
        taskType.setId(dto.getId());
        taskType.setCode(dto.getCode());
        taskType.setName(dto.getName());
        taskType.setExamFamilyId(dto.getExamFamilyId());
        taskType.setSkillId(dto.getSkillId());
        taskType.setDescription(dto.getDescription());
        taskType.setConfigSchema(dto.getConfigSchema());
        taskType.setVersion(dto.getVersion());
        taskType.setCreatedAt(dto.getCreatedAt());
        taskType.setUpdatedAt(dto.getUpdatedAt());
        return taskType;
    }
}

