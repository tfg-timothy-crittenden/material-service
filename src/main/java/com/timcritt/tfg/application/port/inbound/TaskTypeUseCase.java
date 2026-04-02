package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.TaskType;
import java.util.Optional;
import java.util.List;

public interface TaskTypeUseCase {
    TaskType createTaskType(TaskType taskType);
    Optional<TaskType> findTaskTypeById(Long id);
    List<TaskType> findAllTaskTypes();
    TaskType updateTaskType(TaskType taskType);
    void deleteTaskType(Long id);
}

