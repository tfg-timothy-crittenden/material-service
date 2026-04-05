package com.timcritt.tfg.infrastructure.service.business;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import com.timcritt.tfg.application.service.business.MaterialAggregationService;

import java.util.List;


public class MaterialAggregationServiceAdapter implements MaterialAggregationUseCase {

    private final MaterialAggregationService materialAggregationService;


    public MaterialAggregationServiceAdapter(MaterialAggregationService materialAggregationService) {
        this.materialAggregationService = materialAggregationService;
    }

    @Override
    public List<MaterialNodeTreeDto> getMaterialNodeTreeBySectionId(Long sectionId) {
        return materialAggregationService.getMaterialNodeTreeBySectionId(sectionId);
    }
}
