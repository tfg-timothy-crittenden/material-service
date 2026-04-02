package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.ExamFamily;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamFamilyJpaEntity;

public class ExamFamilyEntityMapper {

    static ExamFamily toDomain(ExamFamilyJpaEntity entity) {
        ExamFamily domain = new ExamFamily();
        domain.setId(entity.getId());
        domain.setCode(entity.getCode());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setVersion(entity.getVersion());
        return domain;
    }

    static ExamFamilyJpaEntity toEntity(ExamFamily domain) {
        ExamFamilyJpaEntity entity = new ExamFamilyJpaEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setVersion(domain.getVersion());
        return entity;
    }

}
