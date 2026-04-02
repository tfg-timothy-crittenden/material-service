package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.ExamFamily;
import com.timcritt.tfg.infrastructure.web.dto.ExamFamilyDto;

public final class ExamFamilyDtoMapper {
    private ExamFamilyDtoMapper() {}

    public static ExamFamilyDto toDto(ExamFamily d) {
        if (d == null) return null;
        ExamFamilyDto dto = new ExamFamilyDto();
        dto.setId(d.getId());
        dto.setCode(d.getCode());
        dto.setName(d.getName());
        dto.setDescription(d.getDescription());
        dto.setVersion(d.getVersion());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }

    public static ExamFamily toDomain(ExamFamilyDto dto) {
        if (dto == null) return null;
        ExamFamily domain = new ExamFamily();
        domain.setId(dto.getId());
        domain.setCode(dto.getCode());
        domain.setName(dto.getName());
        domain.setDescription(dto.getDescription());
        domain.setVersion(dto.getVersion());
        domain.setCreatedAt(dto.getCreatedAt());
        domain.setUpdatedAt(dto.getUpdatedAt());
        return domain;
    }
}
