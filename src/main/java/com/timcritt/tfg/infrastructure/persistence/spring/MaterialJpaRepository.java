package com.timcritt.tfg.infrastructure.persistence.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import java.util.List;

public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    List<MaterialJpaEntity> findByExamFamilyId(Long examFamilyId);
}
