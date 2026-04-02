package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.TaskTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskTypeJpaRepository extends JpaRepository<TaskTypeJpaEntity, Long> {
}

