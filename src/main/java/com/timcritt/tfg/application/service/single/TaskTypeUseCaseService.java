package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.TaskTypeUseCase;
import com.timcritt.tfg.application.port.outbound.TaskTypeRepositoryPort;
import com.timcritt.tfg.domain.model.TaskType;
import java.util.List;
import java.util.Optional;

public class TaskTypeUseCaseService implements TaskTypeUseCase {
    private final TaskTypeRepositoryPort repository;

    public TaskTypeUseCaseService(TaskTypeRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TaskType> findTaskTypeById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TaskType> findAllTaskTypes() {
        return repository.findAll();
    }
}

