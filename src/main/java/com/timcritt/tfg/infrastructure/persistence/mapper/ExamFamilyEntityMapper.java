package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.ExamFamily;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamFamilyJpaEntity;

public class ExamFamilyEntityMapper {

    public static ExamFamily toDomain(ExamFamilyJpaEntity entity) {
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


}
