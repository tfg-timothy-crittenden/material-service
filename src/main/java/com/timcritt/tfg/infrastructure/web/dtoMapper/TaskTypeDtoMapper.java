package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.TaskType;
import com.timcritt.tfg.infrastructure.web.dto.TaskTypeDto;

public class TaskTypeDtoMapper {
    public static TaskTypeDto toDto(TaskType taskType) {
        if (taskType == null) return null;
        return TaskTypeDto.builder()
                .id(taskType.getId())
                .name(taskType.getName())
                .description(taskType.getDescription())
                .examFamilyId(taskType.getExamFamilyId())
                .skillId(taskType.getSkillId())
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
        taskType.setName(dto.getName());
        taskType.setDescription(dto.getDescription());
        taskType.setExamFamilyId(dto.getExamFamilyId());
        taskType.setSkillId(dto.getSkillId());
        taskType.setConfigSchema(dto.getConfigSchema());
        taskType.setVersion(dto.getVersion());
        taskType.setCreatedAt(dto.getCreatedAt());
        taskType.setUpdatedAt(dto.getUpdatedAt());
        return taskType;
    }
}
