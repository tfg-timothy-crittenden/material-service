package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.Skill;
import com.timcritt.tfg.infrastructure.web.dto.SkillDto;

public class SkillDtoMapper {
    public static SkillDto toDto(Skill skill) {
        if (skill == null) return null;
        return SkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .version(skill.getVersion())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    public static Skill toDomain(SkillDto dto) {
        if (dto == null) return null;
        Skill skill = new Skill();
        skill.setId(dto.getId());
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill.setVersion(dto.getVersion());
        skill.setCreatedAt(dto.getCreatedAt());
        skill.setUpdatedAt(dto.getUpdatedAt());
        return skill;
    }
}
