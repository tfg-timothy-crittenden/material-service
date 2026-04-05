package com.timcritt.tfg.infrastructure.service.single;

import com.timcritt.tfg.application.port.inbound.TaskTypeUseCase;
import com.timcritt.tfg.application.port.outbound.TaskTypeRepositoryPort;
import com.timcritt.tfg.application.service.single.TaskTypeUseCaseService;
import com.timcritt.tfg.domain.model.TaskType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskTypeServiceAdapter implements TaskTypeUseCase {
    private final TaskTypeUseCaseService delegate;

    public TaskTypeServiceAdapter(TaskTypeRepositoryPort repository) {
        this.delegate = new TaskTypeUseCaseService(repository);
    }

    @Override
    @Transactional
    public TaskType createTaskType(TaskType taskType) {
        return delegate.createTaskType(taskType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskType> findTaskTypeById(Long id) {
        return delegate.findTaskTypeById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskType> findAllTaskTypes() {
        return delegate.findAllTaskTypes();
    }

    @Override
    @Transactional
    public TaskType updateTaskType(TaskType taskType) {
        return delegate.updateTaskType(taskType);
    }

    @Override
    @Transactional
    public void deleteTaskType(Long id) {
        delegate.deleteTaskType(id);
    }
}

