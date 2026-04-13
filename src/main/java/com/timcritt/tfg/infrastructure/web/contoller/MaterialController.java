package com.timcritt.tfg.infrastructure.web.contoller;
import com.timcritt.tfg.application.port.inbound.MaterialUseCase;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.MaterialDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {
    private final MaterialUseCase useCase;
    public MaterialController(MaterialUseCase useCase) {
        this.useCase = useCase;
    }
    @PostMapping
    public ResponseEntity<MaterialDto> create(@RequestBody MaterialDto dto) {
        return ResponseEntity.ok(MaterialDtoMapper.toDto(useCase.createMaterial(MaterialDtoMapper.toDomain(dto))));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MaterialDto> getById(@PathVariable Long id) {
        Optional<com.timcritt.tfg.domain.model.Material> found = useCase.findMaterialById(id);
        return found.map(material -> ResponseEntity.ok(MaterialDtoMapper.toDto(material)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<MaterialDto> update(@PathVariable Long id, @RequestBody MaterialDto dto) {
        com.timcritt.tfg.domain.model.Material toUpdate = MaterialDtoMapper.toDomain(dto);
        toUpdate.setId(id);
        com.timcritt.tfg.domain.model.Material updated = useCase.updateMaterial(toUpdate);
        return ResponseEntity.ok(MaterialDtoMapper.toDto(updated));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/by-exam-family/{examFamilyId}")
    public ResponseEntity<List<MaterialDto>> getByExamFamily(@PathVariable Long examFamilyId) {
        List<com.timcritt.tfg.domain.model.Material> materials = useCase.findByExamFamilyId(examFamilyId);
        List<MaterialDto> dtos = materials.stream().map(MaterialDtoMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }
    @GetMapping
    public ResponseEntity<List<MaterialDto>> getAll() {
        List<com.timcritt.tfg.domain.model.Material> materials = useCase.findAll();
        List<MaterialDto> dtos = materials.stream().map(MaterialDtoMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

}
