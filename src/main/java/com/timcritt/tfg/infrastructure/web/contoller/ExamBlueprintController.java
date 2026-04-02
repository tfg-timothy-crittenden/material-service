package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintUseCase;
import com.timcritt.tfg.domain.model.ExamBlueprint;
import com.timcritt.tfg.infrastructure.web.dtoMapper.ExamBlueprintDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.ExamBlueprintDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam-blueprints")
public class ExamBlueprintController {
    private final ExamBlueprintUseCase useCase;

    public ExamBlueprintController(ExamBlueprintUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ExamBlueprintDto> create(@RequestBody ExamBlueprintDto dto) {
        ExamBlueprint created = useCase.createExamBlueprint(ExamBlueprintDtoMapper.toDomain(dto));
        return ResponseEntity.ok(ExamBlueprintDtoMapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamBlueprintDto> getById(@PathVariable Long id) {
        Optional<ExamBlueprint> found = useCase.findExamBlueprintById(id);
        return found.map(examBlueprint -> ResponseEntity.ok(ExamBlueprintDtoMapper.toDto(examBlueprint)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ExamBlueprintDto> getAll() {
        return useCase.findAllExamBlueprints().stream()
                .map(ExamBlueprintDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamBlueprintDto> update(@PathVariable Long id, @RequestBody ExamBlueprintDto dto) {
        ExamBlueprint toUpdate = ExamBlueprintDtoMapper.toDomain(dto);
        toUpdate.setId(id);
        ExamBlueprint updated = useCase.updateExamBlueprint(toUpdate);
        return ResponseEntity.ok(ExamBlueprintDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteExamBlueprint(id);
        return ResponseEntity.noContent().build();
    }
}

