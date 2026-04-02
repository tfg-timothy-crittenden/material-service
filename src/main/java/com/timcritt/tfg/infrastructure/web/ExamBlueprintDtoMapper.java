package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.ExamBlueprint;
import com.timcritt.tfg.infrastructure.web.dto.ExamBlueprintDto;

public class ExamBlueprintDtoMapper {
    public static ExamBlueprintDto toDto(ExamBlueprint examBlueprint) {
        if (examBlueprint == null) return null;
        return ExamBlueprintDto.builder()
                .id(examBlueprint.getId())
                .examFamilyId(examBlueprint.getExamFamilyId())
                .code(examBlueprint.getCode())
                .name(examBlueprint.getName())
                .versionNo(examBlueprint.getVersionNo())
                .isActive(examBlueprint.getIsActive())
                .version(examBlueprint.getVersion())
                .createdAt(examBlueprint.getCreatedAt())
                .updatedAt(examBlueprint.getUpdatedAt())
                .build();
    }

    public static ExamBlueprint toDomain(ExamBlueprintDto dto) {
        if (dto == null) return null;
        return ExamBlueprint.builder()
                .id(dto.getId())
                .examFamilyId(dto.getExamFamilyId())
                .code(dto.getCode())
                .name(dto.getName())
                .versionNo(dto.getVersionNo())
                .isActive(dto.getIsActive())
                .version(dto.getVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

