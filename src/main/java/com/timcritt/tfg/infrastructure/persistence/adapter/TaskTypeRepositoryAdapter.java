package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.TaskTypeRepositoryPort;
import com.timcritt.tfg.domain.model.TaskType;
import com.timcritt.tfg.infrastructure.persistence.jpa.TaskTypeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.TaskTypeJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TaskTypeRepositoryAdapter implements TaskTypeRepositoryPort {
    private final TaskTypeJpaRepository jpaRepository;

    public TaskTypeRepositoryAdapter(TaskTypeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TaskType save(TaskType taskType) {
        TaskTypeJpaEntity entity = TaskTypeEntityMapper.toEntity(taskType);
        TaskTypeJpaEntity saved = jpaRepository.save(entity);
        return TaskTypeEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<TaskType> findById(Long id) {
        return jpaRepository.findById(id).map(TaskTypeEntityMapper::toDomain);
    }

    @Override
    public List<TaskType> findAll() {
        return jpaRepository.findAll().stream()
                .map(TaskTypeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

