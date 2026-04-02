package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintNodeUseCase;
import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import com.timcritt.tfg.infrastructure.web.dto.ExamBlueprintNodeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam-blueprint-nodes")
public class ExamBlueprintNodeController {
    private final ExamBlueprintNodeUseCase useCase;

    public ExamBlueprintNodeController(ExamBlueprintNodeUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ExamBlueprintNodeDto> create(@RequestBody ExamBlueprintNodeDto dto) {
        ExamBlueprintNode created = useCase.createExamBlueprintNode(ExamBlueprintNodeDtoMapper.toDomain(dto));
        return ResponseEntity.ok(ExamBlueprintNodeDtoMapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamBlueprintNodeDto> getById(@PathVariable Long id) {
        Optional<ExamBlueprintNode> found = useCase.findExamBlueprintNodeById(id);
        return found.map(node -> ResponseEntity.ok(ExamBlueprintNodeDtoMapper.toDto(node)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ExamBlueprintNodeDto> getAll() {
        return useCase.findAllExamBlueprintNodes().stream()
                .map(ExamBlueprintNodeDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamBlueprintNodeDto> update(@PathVariable Long id, @RequestBody ExamBlueprintNodeDto dto) {
        ExamBlueprintNode toUpdate = ExamBlueprintNodeDtoMapper.toDomain(dto);
        toUpdate.setId(id);
        ExamBlueprintNode updated = useCase.updateExamBlueprintNode(toUpdate);
        return ResponseEntity.ok(ExamBlueprintNodeDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteExamBlueprintNode(id);
        return ResponseEntity.noContent().build();
    }
}

