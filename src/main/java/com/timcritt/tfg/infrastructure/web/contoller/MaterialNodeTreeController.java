package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/material-tree")
public class MaterialNodeTreeController {
    private final MaterialAggregationUseCase materialAggregationUseCase;

    public MaterialNodeTreeController(MaterialAggregationUseCase materialAggregationUseCase) {
        this.materialAggregationUseCase = materialAggregationUseCase;
    }

    @GetMapping("/section/{sectionId}")
    public List<MaterialNodeTreeDto> getMaterialNodeTree(@PathVariable Long sectionId) {
        return materialAggregationUseCase.getMaterialNodeTreeBySectionId(sectionId);
    }
}

