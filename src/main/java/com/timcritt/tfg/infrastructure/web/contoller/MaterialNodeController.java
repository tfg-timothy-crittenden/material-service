package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.port.inbound.MaterialAssetUseCase;
import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.service.MaterialNodeServiceAdapter;
import com.timcritt.tfg.infrastructure.web.dto.MaterialAssetDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RestController
    @RequestMapping("/api/material-assets")
    public static class MaterialAssetController {
        @Autowired
        private MaterialAssetUseCase useCase;

        @GetMapping("/{id}")
        public MaterialAssetDto getById(@PathVariable Long id) {
            Optional<MaterialAsset> asset = useCase.getById(id);
            return asset.map(MaterialAssetDto::fromDomain).orElse(null);
        }

        @PostMapping
        public MaterialAssetDto create(@RequestBody MaterialAssetDto dto) {
            MaterialAsset saved = useCase.save(dto.toDomain());
            return MaterialAssetDto.fromDomain(saved);
        }
    }
}

