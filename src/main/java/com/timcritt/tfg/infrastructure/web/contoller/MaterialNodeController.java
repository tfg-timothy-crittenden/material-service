package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.service.single.MaterialNodeServiceAdapter;
import com.timcritt.tfg.infrastructure.web.dto.MaterialAssetDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialAssetDtoMapper;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/material-nodes")
public class MaterialNodeController {
    private final MaterialNodeServiceAdapter service;

    public MaterialNodeController(MaterialNodeServiceAdapter service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MaterialNodeDto> create(@RequestBody MaterialNodeDto dto) {
        MaterialNode created = service.create(MaterialNodeDtoMapper.toDomain(dto));
        return ResponseEntity.ok(MaterialNodeDtoMapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialNodeDto> getById(@PathVariable Long id) {
        Optional<MaterialNode> node = service.findById(id);
        return node.map(n -> ResponseEntity.ok(MaterialNodeDtoMapper.toDto(n)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<MaterialNodeDto> getAll() {
        return service.findAll().stream().map(MaterialNodeDtoMapper::toDto).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialNodeDto> update(@PathVariable Long id, @RequestBody MaterialNodeDto dto) {
        MaterialNode updated = service.update(MaterialNodeDtoMapper.toDomain(dto));
        return ResponseEntity.ok(MaterialNodeDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/kind/{kind}")
    public List<MaterialNodeDto> getByKind(@PathVariable String kind) {
        return service.findByKind(kind).stream().map(MaterialNodeDtoMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<MaterialNodeDto> getByKindExamFamilyAndSkill(
            @RequestParam String kind,
            @RequestParam Long examFamilyId,
            @RequestParam(required = false) Long skillId) {
        return service.findByKindAndExamFamilyIdAndSkillId(kind, examFamilyId, skillId)
                .stream()
                .map(MaterialNodeDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-parent-id-and-display-order")
    public ResponseEntity<MaterialNodeDto> getByParentIdAndDisplayOrder(
            @RequestParam Long parentId,
            @RequestParam Integer displayOrder) {
        Optional<MaterialNode> node = service.findByParentIdAndDisplayOrder(parentId, displayOrder);
        if (node.isPresent()) {
            MaterialNodeDto dto = MaterialNodeDtoMapper.toDto(node.get());
            // Fetch associated material assets if any
            List<MaterialAsset> assets = service.findMaterialAssetsByMaterialNodeId(dto.getId());
            List<MaterialAssetDto> assetDtos = assets.stream().map(MaterialAssetDtoMapper::toDto).collect(Collectors.toList());
            dto.setMaterialAssets(assetDtos);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<MaterialAssetDto>> getAssetsByMaterialNodeId(@PathVariable Long id) {
        List<MaterialAsset> assets = service.findMaterialAssetsByMaterialNodeId(id);
        List<MaterialAssetDto> assetDtos = assets.stream()
                .map(MaterialAssetDtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(assetDtos);
    }
}
