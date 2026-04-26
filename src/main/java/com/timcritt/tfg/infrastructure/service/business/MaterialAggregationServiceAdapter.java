package com.timcritt.tfg.infrastructure.service.business;

import com.timcritt.tfg.application.dto.MaterialNodeTreeDto;
import com.timcritt.tfg.application.port.inbound.MaterialAggregationUseCase;
import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.application.service.business.MaterialAggregationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialAggregationServiceAdapter implements MaterialAggregationUseCase {

    private final MaterialAggregationService materialAggregationService;

    public MaterialAggregationServiceAdapter(MaterialNodeRepositoryPort materialNodeRepositoryPort) {
        this.materialAggregationService = new MaterialAggregationService(materialNodeRepositoryPort);
    }

    @Override
    public List<MaterialNodeTreeDto> getMaterialNodeTreeBySectionId(Long sectionId) {
        return materialAggregationService.getMaterialNodeTreeBySectionId(sectionId);
    }
}
