package com.timcritt.tfg.infrastructure.persistence.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;
import java.util.List;
import java.util.Optional;

public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    List<MaterialJpaEntity> findByExamFamilyId(Long examFamilyId);
    Optional<MaterialJpaEntity> findByMaterialNodeId(Long materialNodeId);
}
