package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/material-aggregation")
public class MaterialAggregationController {
    private final MaterialAggregationUseCase useCase;

    public MaterialAggregationController(MaterialAggregationUseCase useCase) {
        this.useCase = useCase;
    }


    @GetMapping("/tree/{sectionId}")
    public ResponseEntity<List<MaterialNodeTreeDto>> getMaterialNodeTree(@PathVariable Long sectionId) {
        List<MaterialNodeTreeDto> tree = useCase.getMaterialNodeTreeBySectionId(sectionId);
        return ResponseEntity.ok(tree);
    }
}

