package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.TaskType;
import java.util.Optional;
import java.util.List;

public interface TaskTypeRepositoryPort {
    TaskType save(TaskType taskType);
    Optional<TaskType> findById(Long id);
    List<TaskType> findAll();
    void deleteById(Long id);
}

