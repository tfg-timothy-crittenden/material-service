package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import com.timcritt.tfg.infrastructure.service.single.MaterialNodeServiceAdapter;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;
import com.timcritt.tfg.infrastructure.web.dtoMapper.MaterialNodeDtoMapper;
import com.timcritt.tfg.domain.model.MaterialNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/material-aggregation")
public class MaterialAggregationController {
    private final MaterialAggregationUseCase useCase;
    private final MaterialNodeServiceAdapter materialNodeServiceAdapter;

    public MaterialAggregationController(MaterialAggregationUseCase useCase, MaterialNodeServiceAdapter materialNodeServiceAdapter) {
        this.useCase = useCase;
        this.materialNodeServiceAdapter = materialNodeServiceAdapter;
    }


    @GetMapping("/tree/{sectionId}")
    public ResponseEntity<List<MaterialNodeTreeDto>> getMaterialNodeTree(@PathVariable Long sectionId) {
        List<MaterialNodeTreeDto> tree = useCase.getMaterialNodeTreeBySectionId(sectionId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/children/{parentNodeId}")
    public ResponseEntity<List<MaterialNodeDto>> getImmediateChildren(@PathVariable Long parentNodeId) {
        List<MaterialNode> children = materialNodeServiceAdapter.findByParentNodeId(parentNodeId);
        List<MaterialNodeDto> dtos = children.stream()
                .map(MaterialNodeDtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
