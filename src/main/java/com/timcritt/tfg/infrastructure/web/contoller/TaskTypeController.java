package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.TaskTypeUseCase;
import com.timcritt.tfg.domain.model.TaskType;
import com.timcritt.tfg.infrastructure.web.dtoMapper.TaskTypeDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.TaskTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task-types")
public class TaskTypeController {
    private final TaskTypeUseCase useCase;

    public TaskTypeController(TaskTypeUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskTypeDto> getById(@PathVariable Long id) {
        Optional<TaskType> found = useCase.findTaskTypeById(id);
        return found.map(taskType -> ResponseEntity.ok(TaskTypeDtoMapper.toDto(taskType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<TaskTypeDto> getAll() {
        return useCase.findAllTaskTypes().stream()
                .map(TaskTypeDtoMapper::toDto)
                .collect(Collectors.toList());
    }
}

