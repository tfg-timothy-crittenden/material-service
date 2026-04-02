package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialNodeJpaRepository extends JpaRepository<MaterialNodeJpaEntity, Long> {
}

