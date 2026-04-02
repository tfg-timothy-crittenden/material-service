package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.Skill;
import com.timcritt.tfg.infrastructure.persistence.jpa.SkillJpaEntity;

public class SkillEntityMapper {
    public static Skill toDomain(SkillJpaEntity entity) {
        if (entity == null) return null;
        Skill skill = new Skill();
        skill.setId(entity.getId());
        skill.setCode(entity.getCode());
        skill.setName(entity.getName());
        skill.setDescription(entity.getDescription());
        skill.setVersion(entity.getVersion());
        skill.setCreatedAt(entity.getCreatedAt());
        skill.setUpdatedAt(entity.getUpdatedAt());
        return skill;
    }

    public static SkillJpaEntity toEntity(Skill domain) {
        if (domain == null) return null;
        return SkillJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
