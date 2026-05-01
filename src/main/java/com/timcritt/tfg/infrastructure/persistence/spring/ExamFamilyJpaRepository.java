package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.ExamFamilyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamFamilyJpaRepository extends JpaRepository<ExamFamilyJpaEntity,Long> {
}
