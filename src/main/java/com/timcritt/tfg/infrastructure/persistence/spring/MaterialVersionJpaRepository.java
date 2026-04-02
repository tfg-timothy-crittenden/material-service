package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialVersionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialVersionJpaRepository extends JpaRepository<MaterialVersionJpaEntity, Long> {
}

