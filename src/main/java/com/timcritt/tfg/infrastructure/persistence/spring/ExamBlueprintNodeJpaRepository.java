package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintNodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamBlueprintNodeJpaRepository extends JpaRepository<ExamBlueprintNodeJpaEntity, Long> {
}

