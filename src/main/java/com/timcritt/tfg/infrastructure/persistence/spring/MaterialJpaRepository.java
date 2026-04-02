package com.timcritt.tfg.infrastructure.persistence.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialJpaEntity;

public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
}
