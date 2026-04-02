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

    @PostMapping
    public ResponseEntity<TaskTypeDto> create(@RequestBody TaskTypeDto dto) {
        TaskType created = useCase.createTaskType(TaskTypeDtoMapper.toDomain(dto));
        return ResponseEntity.ok(TaskTypeDtoMapper.toDto(created));
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

    @PutMapping("/{id}")
    public ResponseEntity<TaskTypeDto> update(@PathVariable Long id, @RequestBody TaskTypeDto dto) {
        TaskType toUpdate = TaskTypeDtoMapper.toDomain(dto);
        toUpdate.setId(id);
        TaskType updated = useCase.updateTaskType(toUpdate);
        return ResponseEntity.ok(TaskTypeDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteTaskType(id);
        return ResponseEntity.noContent().build();
    }
}

