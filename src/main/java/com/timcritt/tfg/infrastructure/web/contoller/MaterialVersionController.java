package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.MaterialVersionUseCase;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialVersionDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.MaterialVersionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/material-versions")

public class MaterialVersionController {
    private final MaterialVersionUseCase useCase;
    public MaterialVersionController(MaterialVersionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<MaterialVersionDto> create(@RequestBody MaterialVersionDto dto) {
        return ResponseEntity.ok(MaterialVersionDtoMapper.toDto(useCase.createMaterialVersion(MaterialVersionDtoMapper.toDomain(dto))));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MaterialVersionDto> getById(@PathVariable Long id) {
        Optional<com.timcritt.tfg.domain.model.MaterialVersion> found = useCase.findMaterialVersionById(id);
        return found.map(materialVersion -> ResponseEntity.ok(MaterialVersionDtoMapper.toDto(materialVersion)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<MaterialVersionDto> update(@PathVariable Long id, @RequestBody MaterialVersionDto dto) {
        MaterialVersionDto updated = MaterialVersionDtoMapper.toDto(useCase.updateMaterialVersion(MaterialVersionDtoMapper.toDomain(dto)));
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Boolean deleted = useCase.deleteMaterialVersion(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

