package com.timcritt.tfg.infrastructure.persistence.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintJpaEntity;
public interface ExamBlueprintJpaRepository extends JpaRepository<ExamBlueprintJpaEntity, Long> {

}



