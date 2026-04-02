package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialAssetJpaRepository extends JpaRepository<MaterialAssetEntity, Long> {
}

