package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import com.timcritt.tfg.infrastructure.web.dto.ExamBlueprintNodeDto;

public class ExamBlueprintNodeDtoMapper {
    public static ExamBlueprintNodeDto toDto(ExamBlueprintNode node) {
        if (node == null) return null;
        return ExamBlueprintNodeDto.builder()
                .id(node.getId())
                .blueprintId(node.getBlueprintId())
                .parentNodeId(node.getParentNodeId())
                .kind(node.getKind())
                .code(node.getCode())
                .title(node.getTitle())
                .displayOrder(node.getDisplayOrder())
                .skillId(node.getSkillId())
                .taskTypeId(node.getTaskTypeId())
                .isRequired(node.getIsRequired())
                .minChildren(node.getMinChildren())
                .maxChildren(node.getMaxChildren())
                .defaultTimeLimitSeconds(node.getDefaultTimeLimitSeconds())
                .defaultPrepTimeSeconds(node.getDefaultPrepTimeSeconds())
                .config(node.getConfig())
                .version(node.getVersion())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }

    public static ExamBlueprintNode toDomain(ExamBlueprintNodeDto dto) {
        if (dto == null) return null;
        return ExamBlueprintNode.builder()
                .id(dto.getId())
                .blueprintId(dto.getBlueprintId())
                .parentNodeId(dto.getParentNodeId())
                .kind(dto.getKind())
                .code(dto.getCode())
                .title(dto.getTitle())
                .displayOrder(dto.getDisplayOrder())
                .skillId(dto.getSkillId())
                .taskTypeId(dto.getTaskTypeId())
                .isRequired(dto.getIsRequired())
                .minChildren(dto.getMinChildren())
                .maxChildren(dto.getMaxChildren())
                .defaultTimeLimitSeconds(dto.getDefaultTimeLimitSeconds())
                .defaultPrepTimeSeconds(dto.getDefaultPrepTimeSeconds())
                .config(dto.getConfig())
                .version(dto.getVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

